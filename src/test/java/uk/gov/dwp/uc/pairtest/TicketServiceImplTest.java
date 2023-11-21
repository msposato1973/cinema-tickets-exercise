package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.*;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class TicketServiceImplTest {

	@InjectMocks
    private  TicketServiceImpl ticketService;
	
	private SeatReservationService seatReservationService = mock(SeatReservationService.class);

	@SuppressWarnings("deprecation")
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	void testPurchaseTicketNotNull() {
		assertNotNull(ticketService);
		assertNotNull(seatReservationService);
	}

	 @Test
	 void testPurchaseTicketInvalidPurchaseException() {
	 	 
	 	
	 	InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
	 			() -> {
	 				TicketPurchaseRequest ticketpurchaseRequest = null;
	 				ticketService.purchaseTickets(ticketpurchaseRequest);
				}
	 	);
	 	
	 	assertEquals("invalid purchase request - Purchase Request", exception.getMessage());
	 	
	 }
	 
	 @Test
	 void testPurchaseTicketInvalidPurchaseExceptionNoitemIdentify() {
	 	 
	 	
	 	InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
	 			() -> {
	 				TicketPurchaseRequest ticketPurchaseRequest = buildEmptyTicketPurchaseRequest(12345);
	 				ticketService.purchaseTickets(ticketPurchaseRequest);
				}
	 	);
	 	
	 	assertEquals("invalid purchase request -  TicketRequests no item identify", exception.getMessage());
	 	
	 }
	 
	 
	 @Test
	 void testPurchaseTicketErrPurchaseRequest() {
		 InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
			 TicketPurchaseRequest ticketPurchaseRequest = buildEmptyTicketRequests(12345);
			 ticketService.purchaseTickets(ticketPurchaseRequest);
        });
		 
		 assertEquals("invalid purchase request - Invalid TicketRequests", exception.getMessage()); 
	 }
	 
	 @Test
	 void testPurchaseGroceriesErrInvalidAccountId() {
		 InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
			 TicketPurchaseRequest ticketPurchaseRequest  = buildEmptyTicketPurchaseRequest(0);
			 ticketService.purchaseTickets(ticketPurchaseRequest);
        });
		 
		 assertEquals("invalid purchase request - Invalid AccountId", exception.getMessage()); 
	 }
	 
	 @Test
	 void testPurchaseTicketErrPurchaseRequestErrLimit() {
		 InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
			 TicketPurchaseRequest ticketPurchaseRequest  = buildTicketPurchaseRequestDataLimitPItem(12345);
			 ticketService.purchaseTickets(ticketPurchaseRequest);
        });
		 
		 assertEquals("Only a maximum of 20 tickets that can be purchased at a time", exception.getMessage()); 
	 }
	 
	 @Test
	 void testPurchaseTicket() {
			assertNotNull(ticketService);
			TicketPurchaseRequest ticketPurchaseRequest = buildTicketPurchaseRequest(12345);
			
			ticketService.purchaseTickets(ticketPurchaseRequest);
			assertEquals(3, ticketPurchaseRequest.getTicketTypeRequests().length);
			assertEquals(5, ticketService.getTotalSeatsToAllocate());
		    assertEquals(70, ticketService.getTotalAmountToPay());
		    
	 }
	 
	 @Test
	 void testPurchaseTicketData() {
			assertNotNull(ticketService);
			TicketPurchaseRequest purchaseRequest = buildTicketPurchaseRequestData(12345);
			
			ticketService.purchaseTickets(purchaseRequest);
			 
			assertEquals(4, purchaseRequest.getTicketTypeRequests().length);
			assertEquals(90,(int) ticketService.getTotalAmountToPay());
			assertEquals(6, ticketService.getTotalSeatsToAllocate());
	 }
	 
	 @Test
	 void testPurchaseTicket_CalculatesCorrectAmount() {
		 TicketPurchaseRequest purchaseRequest = buildRequest(12345L);
			ticketService.purchaseTickets(purchaseRequest);
			 
			assertNotNull(ticketService);
	 }
	 
	 @Test
	 void testPurchaseTicket_CalculatesCorrectAmountOneINFANT() {
		    TicketPurchaseRequest purchaseRequest = buildTicketPurchaseRequestOneInfant(5L);
		    ticketService.purchaseTickets(purchaseRequest);
			 
			assertNotNull(ticketService);
			assertEquals(3, purchaseRequest.getTicketTypeRequests().length);
			assertEquals(70,(int) ticketService.getTotalAmountToPay());
			assertEquals(5, ticketService.getTotalSeatsToAllocate());
	 }
	 
	 @Test
	 void testPurchaseGroceries_CalculatesCorrectAmountZeroADULTandINFANT() {
		
		    InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
		    	 TicketPurchaseRequest purchaseRequest = buildGroceryPurchaseRequestZeroAdultandINFANT(12345L);
				 ticketService.purchaseTickets(purchaseRequest);
	        });	
		    
		    assertEquals("Child tickets cannot be purchased without purchasing an Adult ticket.", exception.getMessage());
			  
	 }
	 
	 @Test
	 void testPurchaseGroceries_CalculatesCorrectAmountZeroCHILD() {
	    TicketPurchaseRequest purchaseRequest = buildTicketPurchaseRequestZeroCHILD(5L);
	    ticketService.purchaseTickets(purchaseRequest);
		 
		assertNotNull(ticketService);
		assertEquals(3,  purchaseRequest.getTicketTypeRequests().length);
		assertEquals(40,(int) ticketService.getTotalAmountToPay());
		assertEquals(2, ticketService.getTotalSeatsToAllocate());
	 }
	 
	 private TicketPurchaseRequest   buildTicketPurchaseRequestZeroCHILD (long accountId) {
		 
		 TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 2);
		 TicketRequest groceryRequestCHILD =  buildTicketRequest(TicketRequest.Type.CHILD, 0);
		 TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 1);

		 TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
		 return new TicketPurchaseRequest(accountId, ticketRequests);
	}
	 
	 private TicketPurchaseRequest   buildGroceryPurchaseRequestZeroAdultandINFANT (long accountId) {
		 
		 TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 0);
		 TicketRequest groceryRequestCHILD =  buildTicketRequest(TicketRequest.Type.CHILD, 3);
		 TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 0);

		 TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
		 
			return new TicketPurchaseRequest(accountId, ticketRequests);
		}
	 
	 private TicketPurchaseRequest   buildTicketPurchaseRequestZeroInfant (long accountId) {
		 
		    TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 2);
		    TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 5);
		    TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 0);

			TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
					 
			return new TicketPurchaseRequest(accountId, ticketRequests);
	  }
	 
	 
	 private TicketPurchaseRequest   buildRequest (long accountId) {
		 
		    TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 2);
		    TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 3);
		    TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 1);

			TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
					 
			return new TicketPurchaseRequest(accountId, ticketRequests);
	  }
	 
	 
	 private TicketPurchaseRequest   buildTicketPurchaseRequestData (long accountId) {
		 
		 TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 3);
		 TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 3);
		 TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 1);
		 TicketRequest groceryRequestINFANT2 = buildTicketRequest(TicketRequest.Type.INFANT, 2);

		 TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT, groceryRequestINFANT2};
					 
		return new TicketPurchaseRequest(accountId, ticketRequests);
			
	 }
	 
	 private TicketPurchaseRequest   buildTicketPurchaseRequestDataLimitPItem (long accountId) {
		 
		 TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 10);
		 TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 14);
		 TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 0);
			 

		 TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
		  return new TicketPurchaseRequest(accountId, ticketRequests);
			
		}
	 
	 private TicketPurchaseRequest buildEmptyTicketRequests(int accountId) {
		    TicketRequest[] ticketRequests = {};
		 
			return new TicketPurchaseRequest(accountId, ticketRequests);
	 }

	 private TicketPurchaseRequest   buildTicketPurchaseRequestOneInfant (long accountId) {
		 
		 TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 2);
		 TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 3);
		 TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 1);
		 TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
		 return new TicketPurchaseRequest(accountId, ticketRequests);
	}
	 
	 private TicketPurchaseRequest    buildTicketPurchaseRequest(long accountId) {
		 
		 TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 2);
		 TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 3);
		 TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 1);
		 TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
		 return new TicketPurchaseRequest(accountId, ticketRequests);
	}

	 
	 private TicketPurchaseRequest buildEmptyTicketPurchaseRequest(long accountId) {
		 
			TicketRequest groceryRequestADULT = buildTicketRequest(TicketRequest.Type.ADULT, 0);
			TicketRequest groceryRequestCHILD = buildTicketRequest(TicketRequest.Type.CHILD, 0);
			TicketRequest groceryRequestINFANT = buildTicketRequest(TicketRequest.Type.INFANT, 0);

			TicketRequest[] ticketRequests = {groceryRequestADULT, groceryRequestCHILD, groceryRequestINFANT};
					 
			return new TicketPurchaseRequest(accountId, ticketRequests);
		 
	}

	 private TicketRequest buildTicketRequest(Type type, int noOfTickets) {
	    TicketRequest.Type ticketItem = type;
		return new TicketRequest(ticketItem, noOfTickets);
	}
	 
	
}