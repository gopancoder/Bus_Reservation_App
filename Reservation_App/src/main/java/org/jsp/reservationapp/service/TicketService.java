package org.jsp.reservationapp.service;

import java.util.Optional;

import org.jsp.reservationapp.dao.BusDao;
import org.jsp.reservationapp.dao.TicketDao;
import org.jsp.reservationapp.dao.UserDao;
import org.jsp.reservationapp.dto.TicketResponse;
import org.jsp.reservationapp.exception.BusNotFoundException;
import org.jsp.reservationapp.exception.UserNotFoundException;
import org.jsp.reservationapp.model.Bus;
import org.jsp.reservationapp.model.Ticket;
import org.jsp.reservationapp.model.User;
import org.jsp.reservationapp.util.AccountStatus;
import org.jsp.reservationapp.util.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
	@Autowired
	private TicketDao ticketDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private BusDao busDao;

	public TicketResponse bookTicket(int userId, int busId, int numberOfSeats) {
		Optional<Bus> recBus = busDao.findById(busId);
		Optional<User> recUser = userDao.findById(userId);
		if (recBus.isEmpty())
			throw new BusNotFoundException("Can't book Ticket as Bus Id is Invalid");

		if (recUser.isEmpty())
			throw new UserNotFoundException("Can't book Ticket as User Id is Invalid");

		User dbUser = recUser.get();
		if (dbUser.getStatus().equals(AccountStatus.IN_ACTIVE.toString()))
			throw new IllegalArgumentException("Please Activate Your Account , Then book tickets");
		Bus dbBus = recBus.get();
		if (dbBus.getAvailableSeats() < numberOfSeats)
			throw new IllegalArgumentException("Insufficient Seats");

		Ticket ticket = new Ticket();
		ticket.setCost(numberOfSeats * dbBus.getCostPerSeat());
		ticket.setStatus(TicketStatus.BOOKED.toString());
		ticket.setBus(dbBus);// Assigning Bus to ticket
		ticket.setUser(dbUser);// Assigning User to ticket
		ticket.setNumberOfSeatsBooked(numberOfSeats);
		dbBus.getBookedTickets().add(ticket);// Adding ticket to bus
		dbUser.getTickets().add(ticket);// Adding Ticket to User
		dbBus.setAvailableSeats(dbBus.getAvailableSeats() - numberOfSeats);
		userDao.saveUser(dbUser);// Updating User
		busDao.saveBus(dbBus);// Updating Bus
		ticket = ticketDao.saveTicket(ticket);
		return mapToTicketResponse(ticket, dbBus, dbUser);
	}

	public TicketResponse mapToTicketResponse(Ticket ticket, Bus bus, User user) {
		TicketResponse ticketResponse = new TicketResponse();
		ticketResponse.setAge(user.getAge());
		ticketResponse.setBusName(bus.getName());
		ticketResponse.setBusNumber(bus.getBus_number());
		ticketResponse.setCost(ticket.getCost());
		ticketResponse.setDateOfBooking(ticket.getDateOfBooking());
		ticketResponse.setDateOfDeparture(bus.getDate_of_departure());
		ticketResponse.setDestination(bus.getTo_location());
		ticketResponse.setGender(user.getGender());
		ticketResponse.setId(ticket.getId());
		ticketResponse.setNumberOfSeatsBooked(ticket.getNumberOfSeatsBooked());
		ticketResponse.setPhone(user.getPhone());
		ticketResponse.setSource(bus.getFrom_location());
		ticketResponse.setStatus(ticket.getStatus());
		ticketResponse.setUsername(user.getName());
		return ticketResponse;
	}
}
