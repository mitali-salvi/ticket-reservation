package com.neu.edu.ticketreservation.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.edu.ticketreservation.bean.UserBean;
import com.neu.edu.ticketreservation.bean.UserProfile;
import com.neu.edu.ticketreservation.bean.movie.Film;
import com.neu.edu.ticketreservation.bean.movie.FilmSession;
import com.neu.edu.ticketreservation.bean.movie.Seat;
import com.neu.edu.ticketreservation.bean.movie.ShowDetails;
import com.neu.edu.ticketreservation.bean.movie.Theatre;
import com.neu.edu.ticketreservation.bean.movie.Transaction;
import com.neu.edu.ticketreservation.bean.wrapper.FilmWrapper;
import com.neu.edu.ticketreservation.bean.wrapper.SeatWrapper;
import com.neu.edu.ticketreservation.bean.wrapper.ShowDetailsWrapper;
import com.neu.edu.ticketreservation.bean.wrapper.TheatreWrapper;
import com.neu.edu.ticketreservation.bean.wrapper.TransactionWrapper;
import com.neu.edu.ticketreservation.config.ElasticsearchConfig;
import com.neu.edu.ticketreservation.dao.UserDao;
import com.neu.edu.ticketreservation.service.MovieService;
import com.neu.edu.ticketreservation.service.StripeService;
import com.neu.edu.ticketreservation.service.UserProfileService;
import com.neu.edu.ticketreservation.util.SecurityUtil;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private MovieService movieService;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    MeterRegistry registry;

    @Autowired
    private ElasticsearchConfig elasticsearchConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(path = "/v1/theatres")
    public ResponseEntity<Object> getTheatres() {
        registry.counter("custom.metrics.counter", "ApiCall", "TheatreGet").increment();
        logger.info("Get theatres");

        List<Theatre> theatreList = movieService.getAllTheatres();
        List<TheatreWrapper> theatreWrapperList = new ArrayList<TheatreWrapper>();
        for (Theatre t : theatreList) {
            theatreWrapperList.add(new TheatreWrapper().copyFromTheatre(t));
        }

        return new ResponseEntity<>(theatreWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/v1/movies")
    public ResponseEntity<Object> getMovies() {
        registry.counter("custom.metrics.counter", "ApiCall", "MoviesGet").increment();
        logger.info("Get movies");

        List<Film> filmList = movieService.getAllMovies();
        List<FilmWrapper> filmWrapperList = new ArrayList<FilmWrapper>();
        for (Film f : filmList) {
            filmWrapperList.add(new FilmWrapper().copyFromFilm(f));
        }

        return new ResponseEntity<>(filmWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/v1/moviesFromTheatre/{theatreId}")
    public ResponseEntity<Object> getMoviesFromTheatre(@PathVariable(value = "theatreId") String theatreId) {
        registry.counter("custom.metrics.counter", "ApiCall", "MoviesFromTheatreGet").increment();
        logger.info("Get movies from theatres:::" + theatreId);
        UserBean userBean = securityUtil.getPrincipal(userDao);
        if (userBean == null) {
            logger.error("No user found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Film> filmList = movieService.getMoviesFromTheatre(Long.parseLong(theatreId));
        List<FilmWrapper> filmWrapperList = new ArrayList<FilmWrapper>();
        for (Film f : filmList) {
            filmWrapperList.add(new FilmWrapper().copyFromFilm(f));
        }

        return new ResponseEntity<>(filmWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/v1/showDetailsFromMovie/{movieId}")
    public ResponseEntity<Object> getShowDetalsFromTheatre(@PathVariable(value = "movieId") String movieId) {
        registry.counter("custom.metrics.counter", "ApiCall", "ShowDetailsFromMovieGet").increment();
        logger.info("Get getShowDetalsFromTheatre:::" + movieId);
        UserBean userBean = securityUtil.getPrincipal(userDao);
        if (userBean == null) {
            logger.error("No user found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<ShowDetails> showDetailsList = movieService.getShowDetailsFromMovie(Long.parseLong(movieId));
        List<ShowDetailsWrapper> showDetailsWrapperList = new ArrayList<ShowDetailsWrapper>();
        for (ShowDetails sd : showDetailsList) {
            ShowDetailsWrapper sdw = new ShowDetailsWrapper().copyFromShowDetails(sd);

            byte[] showDetailsMapper = new byte[0];
            try {
                showDetailsMapper = objectMapper.writeValueAsBytes(sdw);
                IndexRequest indexRequest = new IndexRequest("showdetails", "_doc",
                        Long.toString(sdw.getFilmSessionId())).source(showDetailsMapper, XContentType.JSON);

                IndexResponse indexResponse = elasticsearchConfig.client().index(indexRequest, RequestOptions.DEFAULT);
                logger.info("Created elasticsearch index: " + indexResponse.getId().toString());
                if (indexResponse.getId().equals("") || indexResponse.getId() == null) {
                    return new ResponseEntity<>("Error creating elasticsearch index", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }

            showDetailsWrapperList.add(sdw);
        }

        return new ResponseEntity<>(showDetailsWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/v1/getMovieLayout")
    public ResponseEntity<Object> getShowLayout(@PathVariable(value = "filmSessionId") long filmSessionId) {
        registry.counter("custom.metrics.counter", "ApiCall", "MovieLayoutGet").increment();
        logger.info("Get getShowLayout::::::"+ filmSessionId);
        UserBean userBean = securityUtil.getPrincipal(userDao);
        if (userBean == null) {
            logger.error("No user found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Seat> seatList = movieService.getAvailableSeats(filmSessionId);
        List<SeatWrapper> seatWrapperList = new ArrayList<SeatWrapper>();
        for (Seat s : seatList) {
            seatWrapperList.add(new SeatWrapper().copyFromSeat(s));
        }

        return new ResponseEntity<>(seatWrapperList, HttpStatus.OK);
    }

    @PostMapping(path = "/v1/bookTickets/{filmSessionId}")
    public ResponseEntity<Object> bookTickets(@RequestBody SeatWrapper[] seats,
            @PathVariable(value = "filmSessionId") long filmSessionId) {
        registry.counter("custom.metrics.counter", "ApiCall", "BookTicketsPost").increment();
        logger.info("Get bookTickets:::" + filmSessionId);
        UserBean userBean = securityUtil.getPrincipal(userDao);
        if (userBean == null) {
            logger.error("No user found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (movieService.checkIfSeatsEmpty(seats)) {
            logger.error("Seats are filled");
            return new ResponseEntity<>("Seats are filled", HttpStatus.BAD_REQUEST);
        }

        FilmSession filmSession = movieService.getFilmSessionFromId(filmSessionId);
        Transaction transaction = movieService.bookTickets(userBean, seats, filmSessionId);

        UserProfile userProfile = userProfileService.getFromUserBean(userBean);
        if (userProfile == null || userProfile.getStripeCustomerId() == null) {
            logger.error("Payment method not found");
            return new ResponseEntity<>("Payment Method not added to account", HttpStatus.BAD_REQUEST);
        }

        String chargeId = stripeService.chargeCard(userBean, userProfile, transaction.getTotalPrice());
        if (chargeId == null) {
            logger.error("Couldnt charge credit card attached to customer");
            return new ResponseEntity<>("Error charging card stored in customers profile", HttpStatus.BAD_REQUEST);
        }

        TransactionWrapper transactionWrapper = new TransactionWrapper();
        return new ResponseEntity<>(transactionWrapper.copyFromTransaction(transaction, filmSession), HttpStatus.OK);
    }
}