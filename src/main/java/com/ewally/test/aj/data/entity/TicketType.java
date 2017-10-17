package com.ewally.test.aj.data.entity;

/**
 * Enumeration of the type of tickets
 *
 * Created by amanjain
 */
public enum TicketType {
    Title_banks("Title Banks"), Payment_dealers("Payment Dealers");
    private String label;
    private TicketType(String label) {this.label = label;}
    public static TicketType findByLabel(String byLabel) {
        for(TicketType tt: TicketType.values()) {
            if(tt.label.equalsIgnoreCase(byLabel))
                return tt;
        }
        return null;
    }
}
