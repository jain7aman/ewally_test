package com.ewally.test.aj.business.service;

import com.ewally.test.aj.business.domain.RawTicket;
import com.ewally.test.aj.data.entity.Ticket;
import com.ewally.test.aj.data.entity.TicketType;
import com.ewally.test.aj.data.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * Ticket Service
 *
 * Created by amanjain
 */
@Service
public class TicketService {

    private TicketRepository ticketRepository;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Creates multiple tickets, if all tickets are valid and new
     *
     * @param rawTicketList
     */
    public void loadTickets(Iterable<RawTicket> rawTicketList) {
        verifyAllTickets(rawTicketList);
        rawTicketList.forEach(rawTicket -> {
            createTicket(rawTicket);
        });
    }

    /**
     * creates single ticket if the ticket code if valid and ticket is new
     *
     * @param rawTicket
     */
    public void createSingleTicket(RawTicket rawTicket) {
        checkIfValidTicketCode(rawTicket.getCode());
        checkIfTicketAlreadyExist(rawTicket.getCode());
        createTicket(rawTicket);
    }

    /**
     *
     * @param ticketCode
     * @return a ticket based on its ticket code
     */
    public Ticket getTicketByTicketCode(String ticketCode) {
        return this.ticketRepository.findByCode(ticketCode);
    }

    /**
     * Checks if the ticket code is valid or not
     *
     * @param ticketCode
     * @return
     */
    public void checkIfValidTicketCode(String ticketCode) throws NoSuchElementException {
        if(!isValidTicketCode(ticketCode)){
            throw new RuntimeException("Invalid ticket with code " + ticketCode);
        }
    }

    private boolean isValidTicketCode(String ticketCode) {
        if (null == ticketCode)
            return false;
        if (ticketCode.length() != 47)
            return false;
        return true;
    }

    private TicketType getTicketType(String ticketCode) {
        // TODO identify ticket type based on its ticket code
        return TicketType.findByLabel("Title Banks");
    }

    private void verifyAllTickets(Iterable<RawTicket> rawTicketList) {
        rawTicketList.forEach(rawTicket -> {
            checkIfValidTicketCode(rawTicket.getCode());
            checkIfTicketAlreadyExist(rawTicket.getCode());
        });
    }

    private void createTicket(RawTicket rawTicket) {
        TicketType ticketType = getTicketType(rawTicket.getCode());
        String barcode = generateBarCode(rawTicket.getCode(), rawTicket.getValue(), rawTicket.getExpDate());
        Date date = this.createDateFromDateString(rawTicket.getExpDate());
        java.sql.Date sqlDate = null;
        if(null != date) {
            sqlDate = new java.sql.Date(date.getTime());
        }
        this.ticketRepository.save(new Ticket(rawTicket.getCode(), rawTicket.getValue(), sqlDate, barcode, ticketType));
    }

    private String generateBarCode(String ticketCode, String value, String expDateString) {
        // TODO generate bar code for the ticket
        return "";
    }

    private Date createDateFromDateString(String dateString) {
        Date date = null;
        if(null != dateString) {
            try {
                date = DATE_FORMAT.parse(dateString);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid ticket date format. Correct format: dd/MM/yyyy (e.g., 29/01/2017)");
            }
        }
        return date;
    }

    private void checkIfTicketAlreadyExist(String ticketCode) throws RuntimeException {
        Ticket ticket = getTicketByTicketCode(ticketCode);
        if (null != ticket) {
            throw new RuntimeException("Ticket already exist with code " + ticketCode);
        }
    }


}
