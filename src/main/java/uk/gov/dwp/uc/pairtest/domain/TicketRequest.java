package uk.gov.dwp.uc.pairtest.domain;

/**
 * Should be an Immutable Object
 */
public class TicketRequest {

    private int noOfTickets;
    private Type type;

    public TicketRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT, CHILD , INFANT
    }
    
    public enum Ticket {
    	ADULT("20"),
    	CHILD("10"),
    	INFANT("0");

        private final String code;
        Ticket(String code){
            this.code = code;
        }

        public String getCode(){
            return code;
        }
    }

}
