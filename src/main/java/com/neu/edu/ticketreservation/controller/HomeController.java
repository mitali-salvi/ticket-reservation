package com.neu.edu.ticketreservation.controller;

import java.util.ArrayList;
import java.util.List;

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
import com.neu.edu.ticketreservation.dao.UserDao;
import com.neu.edu.ticketreservation.service.MovieService;
import com.neu.edu.ticketreservation.service.StripeService;
import com.neu.edu.ticketreservation.service.UserProfileService;
import com.neu.edu.ticketreservation.util.SecurityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(path = "/theatres")
    public ResponseEntity<Object> getTheatres() {
        logger.info("Get theatres");

        List<Theatre> theatreList = movieService.getAllTheatres();
        List<TheatreWrapper> theatreWrapperList = new ArrayList<TheatreWrapper>();
        for (Theatre t : theatreList) {
            theatreWrapperList.add(new TheatreWrapper().copyFromTheatre(t));
        }

        return new ResponseEntity<>(theatreWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/movies")
    public ResponseEntity<Object> getMovies() {
        logger.info("Get movies");

        List<Film> filmList = movieService.getAllMovies();
        List<FilmWrapper> filmWrapperList = new ArrayList<FilmWrapper>();
        for (Film f : filmList) {
            filmWrapperList.add(new FilmWrapper().copyFromFilm(f));
        }

        return new ResponseEntity<>(filmWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/moviesFromTheatre/{theatreId}")
    public ResponseEntity<Object> getMoviesFromTheatre(Authentication authentication, @PathVariable(value = "theatreId") String theatreId) {
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

    @GetMapping(path = "/showDetailsFromMovie/{movieId}")
    public ResponseEntity<Object> getShowDetalsFromTheatre(Authentication authentication, @PathVariable(value = "movieId") String movieId) {
        logger.info("Get getShowDetalsFromTheatre:::" + movieId);
        UserBean userBean = securityUtil.getPrincipal(userDao);
        if (userBean == null) {
            logger.error("No user found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<ShowDetails> showDetailsList = movieService.getShowDetailsFromMovie(Long.parseLong(movieId));
        List<ShowDetailsWrapper> showDetailsWrapperList = new ArrayList<ShowDetailsWrapper>();
        for (ShowDetails sd : showDetailsList) {
            showDetailsWrapperList.add(new ShowDetailsWrapper().copyFromShowDetails(sd));
        }

        return new ResponseEntity<>(showDetailsWrapperList, HttpStatus.OK);
    }

    @GetMapping(path = "/getMovieLayout/{filmSessionId}")
    public ResponseEntity<Object> getShowLayout(Authentication authentication,
        @PathVariable(value = "filmSessionId") long filmSessionId) {
        logger.info("Get getShowLayout::" + filmSessionId);
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

    @PostMapping(path = "/bookTickets/{filmSessionId}")
    public ResponseEntity<Object> bookTickets(Authentication authentication, @RequestBody SeatWrapper[] seats,
            @PathVariable(value = "filmSessionId") long filmSessionId) {
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
        if (userProfile ==null || userProfile.getStripeCustomerId() ==null) {
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