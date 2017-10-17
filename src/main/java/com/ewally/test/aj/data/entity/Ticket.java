package com.ewally.test.aj.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Date;

/**
 * The ticket contains all the attributes of Ewally Test Ticket
 *
 * Created by amanjain
 */
@Entity
public class Ticket implements Serializable {
    @Id
    @Column(name = "CODE", length = 47)
    private String code;

    @Column(name = "VALUE", nullable = true)
    private String value;

    @Column(name = "EXPIRATION_DATE", nullable = true)
    private Date expirationDate;

    @Column(name = "BAR_CODE", length = 44, nullable = false)
    private String barcode;

    @Column(name = "TYPE", nullable = false)
    private TicketType ticketType;

    protected Ticket() {
    }

    public Ticket(String code, String value, Date expirationDate, String barcode, TicketType ticketType) {
        this.code = code;
        this.value = value;
        this.expirationDate = expirationDate;
        this.barcode = barcode;
        this.ticketType = ticketType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ticket ticket = (Ticket) o;

        if (!code.equals(ticket.code)) return false;
        if (value != null ? !value.equals(ticket.value) : ticket.value != null) return false;
        if (expirationDate != null ? !expirationDate.equals(ticket.expirationDate) : ticket.expirationDate != null)
            return false;
        if (!barcode.equals(ticket.barcode)) return false;
        return ticketType == ticket.ticketType;
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (expirationDate != null ? expirationDate.hashCode() : 0);
        result = 31 * result + barcode.hashCode();
        result = 31 * result + ticketType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "code='" + code + '\'' +
                ", value='" + value + '\'' +
                ", expirationDate=" + expirationDate +
                ", barcode='" + barcode + '\'' +
                ", ticketType=" + ticketType +
                '}';
    }
}
