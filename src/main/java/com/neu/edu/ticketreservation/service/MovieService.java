package com.neu.edu.ticketreservation.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.neu.edu.ticketreservation.bean.UserBean;
import com.neu.edu.ticketreservation.bean.movie.Film;
import com.neu.edu.ticketreservation.bean.movie.FilmSession;
import com.neu.edu.ticketreservation.bean.movie.Hall;
import com.neu.edu.ticketreservation.bean.movie.Row;
import com.neu.edu.ticketreservation.bean.movie.Seat;
import com.neu.edu.ticketreservation.bean.movie.ShowDetails;
import com.neu.edu.ticketreservation.bean.movie.Theatre;
import com.neu.edu.ticketreservation.bean.movie.Ticket;
import com.neu.edu.ticketreservation.bean.movie.Transaction;
import com.neu.edu.ticketreservation.bean.wrapper.SeatWrapper;
import com.neu.edu.ticketreservation.bean.wrapper.ShowDetailsWrapper;
import com.neu.edu.ticketreservation.dao.FilmDao;
import com.neu.edu.ticketreservation.dao.FilmSessionDao;
import com.neu.edu.ticketreservation.dao.SeatDao;
import com.neu.edu.ticketreservation.dao.TheatreDao;
import com.neu.edu.ticketreservation.dao.TicketDao;
import com.neu.edu.ticketreservation.dao.TransactionDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${SERVICE_CHARGE}")
    private long serviceCharge;

    @Autowired
	private TheatreDao theatreDao;

	@Autowired
	private FilmDao filmDao;

	@Autowired
	private FilmSessionDao filmSessionDao;

	@Autowired
	private SeatDao seatDao;

	@Autowired
	private TicketDao tickeDao;

	@Autowired
	private TransactionDao transactionDao;
    
    public List<Theatre> getAllTheatres() {
		return theatreDao.findAll();
	}

	public List<Film> getMoviesFromTheatre(long theatreId) {
		return filmDao.getMoviesFromTheatre(theatreId);
	}

	public List<Film> getAllMovies() {
		return filmDao.findAll();
	}

	public FilmSession getFilmSessionFromId(long filmSessionId) {
		return filmSessionDao.getFilmByFilmSessionId(filmSessionId);
	}

	public List<ShowDetails> getShowDetailsFromMovie(long filmId){

		Film film = filmDao.getFilmByFilmId(filmId);
		List<FilmSession> filmSessions = filmSessionDao.findByFilm(film);

		List<ShowDetails> showDetailsList = new ArrayList<ShowDetails>();

		for (FilmSession fs: filmSessions){
			ShowDetails showDetail = new ShowDetails();
			Hall hall = fs.getHall();
			Theatre theatre = hall.getTheatre();

			showDetail.setFilm(film);
			showDetail.setFilmSession(fs);
			showDetail.setTheatre(theatre);

			showDetailsList.add(showDetail);
		}

		return showDetailsList;
	}

	public List<Seat> getAvailableSeats (long filmSessionId){
		FilmSession filmSession = filmSessionDao.getFilmByFilmSessionId(filmSessionId);
		Hall hall = filmSession.getHall();

		Set<Row> rows = hall.getRows();

		List<Seat> seatList = new ArrayList<Seat>();
		for (Row row: rows){
			List<Seat> seats = seatDao.findByRow(row);
			for(Seat s: seats){
				if (!s.isSeatBooked())
					seatList.add(s);
			}
		}

		return seatList;
	}

	public Transaction bookTickets (UserBean userBean, SeatWrapper[] seats, long filmSessionId){
		List<Seat> seatList = new ArrayList<Seat>();
		for (SeatWrapper s: seats){
			Seat seat =seatDao.findBySeatId(s.getSeatNumber());
			seat.setSeatBooked(true);
			seatList.add(seat);
			seatDao.save(seat);
		}

		double totalPrice =0;
		FilmSession filmSession = filmSessionDao.getFilmByFilmSessionId(filmSessionId);
		Transaction transaction = new Transaction();
		Set<Ticket> ticketList = new HashSet<Ticket>();
	
		for(Seat s: seatList){
			Ticket ticket = new Ticket();
			ticket.setFilmSession(filmSession);
			ticket.setSeat(s);
			ticket.setTransaction(transaction);
			tickeDao.save(ticket);

			ticketList.add(ticket);
			totalPrice += s.getPrice();
		}
		
		totalPrice = totalPrice + ((serviceCharge*totalPrice)/100); 
		logger.info("Total Price::"+ totalPrice);
		transaction.setTotalPrice(totalPrice);
		transaction.setUserBean(userBean);
		transaction.setTickets(ticketList);
		transactionDao.save(transaction);

		return transaction;
	}

	public boolean checkIfSeatsEmpty (SeatWrapper[] seats){
		for (SeatWrapper s: seats){
			Seat seat =seatDao.findBySeatId(s.getSeatNumber());
			if(seat.isSeatBooked()){
				return true;
			}
		}

		return false;
	}

	public List<Transaction> getPastTransactions (UserBean userBean) {
		
		return transactionDao.findByUserBean(userBean);
	}

}