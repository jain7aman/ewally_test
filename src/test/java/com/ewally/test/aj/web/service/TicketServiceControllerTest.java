package com.ewally.test.aj.web.service;

import com.ewally.test.TicketInfoApplication;
import com.ewally.test.aj.business.domain.RawTicket;
import com.ewally.test.aj.data.entity.Ticket;
import com.ewally.test.aj.data.entity.TicketType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.awt.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Testing the TicketServiceController
 *
 * Created by amanjain
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TicketInfoApplication.class)
@ActiveProfiles("test")
public class TicketServiceControllerTest {
    private static final String URI = "http://localhost:8080/ticket";

    private static final DateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat DATE_FORMAT_2 = new SimpleDateFormat("dd/MM/yyyy");


    @InjectMocks
    private TicketServiceController controller;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void initTests() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldCreateRetrieveTest() throws Exception {
        String ticketCode = "23792856266000000025152001324806173140000002000";
        List<RawTicket> rawTickets = mockRawTicketList(new String[] {ticketCode});
        byte[] rawTicketsJson = toJson(rawTickets);

        //CREATE
        MvcResult result = mockMvc.perform(post("/ticket")
                .content(rawTicketsJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //RETRIEVE
        mockMvc.perform(get("/ticket/" + ticketCode)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ticketCode)))
                .andExpect(jsonPath("$.value", is(rawTickets.get(0).getValue())))
                .andExpect(jsonPath("$.expirationDate",
                        is(DATE_FORMAT_1.format(createDateFromDateString(rawTickets.get(0).getExpDate())))));

    }

    @Test
    public void shouldHaveProperDateFormatTest() throws Exception {
        String ticketCode = "23792856266000000025152001324806173140000002000";
        RawTicket mockRawTicket = mockRawTicket(ticketCode, "40000", "29-01-2012");
        List<RawTicket> mockRawTickets = new ArrayList<>();
        mockRawTickets.add(mockRawTicket);
        byte[] rawTicketsJson = toJson(mockRawTickets);
        mockMvc.perform(post("/ticket")
                .content(rawTicketsJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void shouldHaveProperTicketCode() throws Exception {
        String incorrectTicketCode = "237928562660000000251520013248061731400000020";
        RawTicket mockRawTicket = mockRawTicket(incorrectTicketCode, "40000", "29/01/2012");
        List<RawTicket> mockRawTickets = new ArrayList<>();
        mockRawTickets.add(mockRawTicket);
        byte[] rawTicketsJson = toJson(mockRawTickets);
        mockMvc.perform(post("/ticket")
                .content(rawTicketsJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void shouldIdentifyProperTicketType() throws Exception {
        // TODO identify tickets based on its types
    }


    /**********************Private Methods*************************/
    private RawTicket mockRawTicket(String code, String value, String expDateString) {
        RawTicket rawTicket = new RawTicket();
        rawTicket.setCode(code);
        rawTicket.setExpDate(expDateString);
        rawTicket.setValue(value);
        return rawTicket;
    }

    private List<RawTicket> mockRawTicketList(String[] codes) {
        Iterable<String> allCodes = Arrays.asList(codes);
        List<RawTicket> rawTickets = new ArrayList<>();
        allCodes.forEach(code->{
            rawTickets.add(mockRawTicket(code, "40000", "22/01/2012"));
        });

        return rawTickets;
    }

    private byte[] toJson(Object r) throws Exception {
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(r).getBytes();
    }

    private java.util.Date createDateFromDateString(String dateString) {
        java.util.Date date = null;
        if(null != dateString) {
            try {
                date = DATE_FORMAT_2.parse(dateString);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid ticket date format. Correct format: dd/MM/yyyy (e.g., 29/01/2017)");
            }
        }
        return date;
    }

}
