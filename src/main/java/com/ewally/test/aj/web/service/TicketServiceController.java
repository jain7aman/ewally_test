package com.ewally.test.aj.web.service;

import com.ewally.test.aj.business.domain.RawTicket;
import com.ewally.test.aj.data.entity.Ticket;
import com.ewally.test.aj.business.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Ticket Controller
 *
 * Created by amanjain
 */
@RestController
@RequestMapping(value = "/ticket")
public class TicketServiceController {

    @Autowired
    private TicketService ticketService;


    /**
     * Creates a single ticket. Ticket is created only if it has valid code and no other ticket with same code exists
     *
     * @param ticketCode
     * @param rawTicket
     */
    @RequestMapping(method = RequestMethod.POST, path = "/{ticketCode}",
            produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    public void createTicket(@PathVariable(value = "ticketCode") String ticketCode,
                             @RequestBody @Validated RawTicket rawTicket) {
        rawTicket.setCode(ticketCode);
        this.ticketService.createSingleTicket(rawTicket);
    }

    /**
     * Creates single or multiple ticket(s). Method will only succeed if all the tickets are valid and new.
     * if any of the ticket is invalid or already existed in our database then none of the tickets are
     * inserted into the database.
     *
     * @param rawTicketList
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    public void loadTickets(@RequestBody @Validated Iterable<RawTicket> rawTicketList) {
        this.ticketService.loadTickets(rawTicketList);
    }

    /**
     * Lookup a ticket
     *
     * @param ticketCode
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{ticketCode}",
            produces = {"application/json", "application/xml"})
    public Ticket getTicketInfo(@PathVariable(value = "ticketCode") String ticketCode) {
        Ticket ticket = verifyTicket(ticketCode);
        return ticket;
    }

    private Ticket verifyTicket(String ticketCode) throws NoSuchElementException {
        this.ticketService.checkIfValidTicketCode(ticketCode);
        Ticket ticket = this.ticketService.getTicketByTicketCode(ticketCode);
        if (null == ticket) {
            throw new NoSuchElementException("Ticket does not exist with code " + ticketCode);
        }
        return ticket;
    }

    /**
     * Exception handler if NoSuchElementException is thrown in this controller
     *
     * @param ex
     * @return Error message string
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String return400(NoSuchElementException ex) {
        return ex.getMessage();
    }

    /**
     * Exception handler if RuntimeException is thrown in this controller
     *
     * @param ex
     * @return Error message string
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public String return400(RuntimeException ex) {
        return ex.getMessage();
    }
}
