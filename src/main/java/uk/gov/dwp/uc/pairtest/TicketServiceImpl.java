package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import java.util.List;

import thirdparty.paymentgateway.*;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

	/**
	 * Should only have private methods other than the one below.
	 */

	private SeatReservationService seatReservationService;
	private TicketPaymentService ticketPaymentService;
	private double totalAmountToPay;
	private int totalAdult;
	private int totalSeatsToAllocate;
	

	public int getTotalSeatsToAllocate() {
		return totalSeatsToAllocate;
	}

	public void setTotalSeatsToAllocate(int totalSeatsToAllocate) {
		this.totalSeatsToAllocate = totalSeatsToAllocate;
	}

	public void setTotalAdult(int totalAdult) {
		this.totalAdult = totalAdult;
	}

	public int getTotalAdult() {
		return totalAdult;
	}

	public double getTotalAmountToPay() {
		return totalAmountToPay;
	}

	public void setTotalAmountToPay(double totalAmountToPay) {
		this.totalAmountToPay = totalAmountToPay;
	}

	public TicketServiceImpl() {
		super();
		this.ticketPaymentService = new TicketPaymentServiceImpl();

		setTotalAmountToPay(initPrice());
		setTotalAdult(initTotalAdult());
		setTotalSeatsToAllocate(initTotalSeatsToAllocate());
	}

	private double initPrice() {
		return 0.0;
	}

	private int initTotalAdult() {
		return 0;
	}

	private int initTotalSeatsToAllocate() {
		return 0;
	}

	@Override
	public void purchaseTickets(final TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
		validationRequest(ticketPurchaseRequest);

		int totalToPay = calculatesPurchase(ticketPurchaseRequest);
		setTotalAmountToPay(totalToPay);

		ticketPaymentService.makePayment(ticketPurchaseRequest.getAccountId(), (int) getTotalAmountToPay());
		seatReservationService.reserveSeat(ticketPurchaseRequest.getAccountId(), getTotalSeatsToAllocate());

	}

	private int calculatesPurchase(TicketPurchaseRequest ticketPurchaseRequest) {
		List<TicketRequest> list = Arrays.asList(ticketPurchaseRequest.getTicketTypeRequests());

		if (limiNumOfTickets(list))
			throw new InvalidPurchaseException("Only a maximum of 20 tickets that can be purchased at a time");

		if (!list.isEmpty()) {
			list.stream().forEach(item -> {
				if (item.getNoOfTickets() > 0) {
					getPriceBYType(item.getTicketType(), item.getNoOfTickets());
				}
			});
		}

		if (getTotalAmountToPay() == 0.0)
			throw new InvalidPurchaseException("Invalid Purchase");

		return (int) getTotalAmountToPay();
	}

	private void checkNumAdult(List<TicketRequest> list) {

		int noOfAdultTickets = list.stream()
				.filter((ticket) -> (ticket.getTicketType() == Type.ADULT) && ticket.getNoOfTickets() > 0)
				.mapToInt(TicketRequest::getNoOfTickets).sum();

		setTotalAdult(noOfAdultTickets);

	}

	private boolean limiNumOfTickets(List<TicketRequest> list) {
		int noOfTickets = list.stream().mapToInt(TicketRequest::getNoOfTickets).sum();
		return (noOfTickets > 20);
	}

	private void validationRequest(TicketPurchaseRequest ticketPurchaseRequest) {

		if (ticketPurchaseRequest == null)
			throw new InvalidPurchaseException("invalid purchase request - Purchase Request");

		if (ticketPurchaseRequest.getAccountId() == 0)
			throw new InvalidPurchaseException("invalid purchase request - Invalid AccountId");

		if (ticketPurchaseRequest.getTicketTypeRequests().length == 0)
			throw new InvalidPurchaseException("invalid purchase request - Invalid TicketRequests");

		if (noTicketRequestsIdentify(Arrays.asList(ticketPurchaseRequest.getTicketTypeRequests())))
			throw new InvalidPurchaseException("invalid purchase request -  TicketRequests no item identify");

		checkNumAdult(Arrays.asList(ticketPurchaseRequest.getTicketTypeRequests()));
	}

	private boolean noTicketRequestsIdentify(List<TicketRequest> asList) {
		return (asList.stream().mapToInt(TicketRequest::getNoOfTickets).sum() == 0);
	}

	private void getPriceBYType(Type ticketType, int noOfTickets) {

		switch (ticketType) {
		case ADULT:
			updateTotalAmountToPayAndTotalSeatsToAllocate(20, noOfTickets);
			break;
		case CHILD:
			if (getTotalAdult() > 0) {
				updateTotalAmountToPayAndTotalSeatsToAllocate(10, noOfTickets);
			} else {
				throw new InvalidPurchaseException(
						"Child tickets cannot be purchased without purchasing an Adult ticket.");
				}
			break;
		case INFANT:
			if (getTotalAdult() < 1) {
				throw new InvalidPurchaseException(
						"Infant tickets cannot be purchased without purchasing an Adult ticket.");
				}
			break;
		default:
			throw new InvalidPurchaseException("Invalid ticketType: " + ticketType);

		}

	}

	private void updateTotalAmountToPayAndTotalSeatsToAllocate(int ticketPrice, int noOfTickets) {
		setTotalAmountToPay(getTotalAmountToPay() + (ticketPrice * noOfTickets));
		setTotalSeatsToAllocate(getTotalSeatsToAllocate() + noOfTickets);
	}
}
