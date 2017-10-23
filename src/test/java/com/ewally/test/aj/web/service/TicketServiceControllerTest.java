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
import java.util.concurrent.TimeUnit;

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
    public void shouldCreateRetrieveMultipleTest() throws Exception {
        String ticketCode1 = "23792856266000000025152001324806173140000002000";
        String ticketCode2 = "23792856266000000025152001324806173140000002001";
        List<RawTicket> rawTickets = mockRawTicketList(new String[] {ticketCode1, ticketCode2});
        byte[] rawTicketsJson = toJson(rawTickets);

        //CREATE
        MvcResult result = mockMvc.perform(post("/ticket")
                .content(rawTicketsJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //RETRIEVE
        mockMvc.perform(get("/ticket/" + ticketCode1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ticketCode1)))
                .andExpect(jsonPath("$.value", is(rawTickets.get(0).getValue())))
                .andExpect(jsonPath("$.expirationDate",
                        is(DATE_FORMAT_1.format(createDateFromDateString(rawTickets.get(0).getExpDate())))));

        mockMvc.perform(get("/ticket/" + ticketCode2)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ticketCode2)))
                .andExpect(jsonPath("$.value", is(rawTickets.get(1).getValue())))
                .andExpect(jsonPath("$.expirationDate",
                        is(DATE_FORMAT_1.format(createDateFromDateString(rawTickets.get(1).getExpDate())))));
    }

    @Test
    public void shouldCreateRetrieveSingleTest() throws Exception {
        String ticketCode = "23792856266000000025152001324806173140000002003";
        RawTicket mockRawTicket = mockRawTicket(null, "40000", "29/01/2012");
        byte[] rawTicketJson = toJson(mockRawTicket);
        mockMvc.perform(post("/ticket/" + ticketCode)
                .content(rawTicketJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void shouldHaveProperDateFormatTest() throws Exception {
        // ticket with value less than 10 digits and date for which Maturity Factor is 9999
        String ticketCode1 = "23792856266000000025152001324806173140000002100";
        RawTicket mockRawTicket1 = mockRawTicket(ticketCode1, "40000", "21/02/2025");
        String expectedBarCode1 = generateBarCode(ticketCode1, "40000", "21/02/2025");

        // ticket with value equal 10 digits and date for which Maturity Factor is 1000
        String ticketCode2 = "23792856266000000025152001324806173140000002200";
        RawTicket mockRawTicket2 = mockRawTicket(ticketCode2, "9999999999", "22/02/2025");
        String expectedBarCode2 = generateBarCode(ticketCode2, "9999999999", "22/02/2025");

        // ticket with value greater than 10 digits and date for which Maturity Factor is 4798
        String ticketCode3 = "23792856266000000025152001324806173140000002300";
        RawTicket mockRawTicket3 = mockRawTicket(ticketCode3, "99999999991", "17/11/2010");
        String expectedBarCode3 = generateBarCode(ticketCode3, "99999999991", "17/11/2010");

        List<RawTicket> mockRawTickets = new ArrayList<>();
        mockRawTickets.add(mockRawTicket1);
        mockRawTickets.add(mockRawTicket2);
        mockRawTickets.add(mockRawTicket3);

        byte[] rawTicketsJson = toJson(mockRawTickets);
        mockMvc.perform(post("/ticket")
                .content(rawTicketsJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //RETRIEVE
        mockMvc.perform(get("/ticket/" + ticketCode1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ticketCode1)))
                .andExpect(jsonPath("$.value", is(mockRawTickets.get(0).getValue())))
                .andExpect(jsonPath("$.expirationDate",
                        is(DATE_FORMAT_1.format(createDateFromDateString(mockRawTickets.get(0).getExpDate())))))
                .andExpect(jsonPath("$.barcode", is(expectedBarCode1)));

        mockMvc.perform(get("/ticket/" + ticketCode2)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ticketCode2)))
                .andExpect(jsonPath("$.value", is(mockRawTickets.get(1).getValue())))
                .andExpect(jsonPath("$.expirationDate",
                        is(DATE_FORMAT_1.format(createDateFromDateString(mockRawTickets.get(1).getExpDate())))))
                .andExpect(jsonPath("$.barcode", is(expectedBarCode2)));

        mockMvc.perform(get("/ticket/" + ticketCode3)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ticketCode3)))
                .andExpect(jsonPath("$.value", is(mockRawTickets.get(2).getValue())))
                .andExpect(jsonPath("$.expirationDate",
                        is(DATE_FORMAT_1.format(createDateFromDateString(mockRawTickets.get(2).getExpDate())))))
                .andExpect(jsonPath("$.barcode", is(expectedBarCode3)));
    }

    @Test
    public void shouldHaveProperTicketCodeTest() throws Exception {
        String incorrectTicketCode = "237928562660000000251520013248061731400000025";
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

//    @Test
//    public void shouldIdentifyProperTicketType() throws Exception {
//        // TODO identify tickets based on its types
//
//
//    }


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

    private String generateBarCode(String ticketCode, String value, String expDateString) {
        java.util.Date expDate = null;
        try {
            expDate = DATE_FORMAT_2.parse(expDateString);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid ticket date format. Correct format: dd/MM/yyyy (e.g., 29/01/2017)");
        }
        int[] barcodeArray = new int[44];

        // position 01 to 03 Bank code in clearing house = 001
        barcodeArray[0] = 0;
        barcodeArray[1] = 0;
        barcodeArray[2] = 1;
        barcodeArray[3] = 9; // Currency Code 9 (Real)
        // barcodeArray[4] = 0; Digital bar code checker, will be calculated in the last
        int maturityFactor = getMaturityFactor(expDate);
        int base = 1000;
        // Position 06 to 09
        for(int i = 0; i < 4; i++) {
            barcodeArray[5 + i] = (maturityFactor/ base);
            maturityFactor = maturityFactor % base;
            base = base / 10;
        }

        // positions 10 to 19
        String valueString = value + "";
        int valueLength = valueString.length();

        if(valueLength > 10) {

            if(valueLength < 15) {
                // Tickets with values ​​higher than R $ 99,999,999.99 should advance
                // on the "Expiration Factor" by eliminating it from the bar code

                // assuming expiration factor is the "Maturity Factor" we calculated previously
                int valueStartingIndex = 9 - (valueLength - 10);
                for(int i = valueStartingIndex, j = 0; i < valueLength; i++, j++){
                    barcodeArray[i] = Integer.parseInt(valueString.charAt(j)+"");
                }

            } else {
                // dont know what to do
            }
        } else { // value length <= 10
            for(int i = 9; i < (10 - valueLength) + 9; i++) {
                barcodeArray[i] = 0;
            }
            for(int i = (10 - valueLength) + 9, j = 0; i < 19; i++, j++) {
                barcodeArray[i] = Integer.parseInt(valueString.charAt(j)+"");;
            }
        }

        // Positions 20 to 44 Free Field
        // TICKET CODE:  AAABC.CCCCX DDDDD.DDDDDY EEEEE.EEEEEZ K UUUUVVVVVVVVVV

        // C =Barcode Positions 20 to 24
        for(int i = 19, j = 0; i < 24; i++, j++) {
            barcodeArray[i] = Integer.parseInt(ticketCode.charAt(4 + j) + "");
        }

        // D = Barcode Positions 25 to 34
        for(int i = 24, j = 0; i < 34; i++, j++) {
            barcodeArray[i] = Integer.parseInt(ticketCode.charAt(10 + j) + "");
        }

        // F =  Barcode Positions 35 to 44 (documentation says its E,
        // perhaps its a misprint, countl'd find more information, need to discuss
        for(int i = 34, j = 0; i < 44; i++, j++) {
            barcodeArray[i] = Integer.parseInt(ticketCode.charAt(21 + j) + "");
        }

        barcodeArray[4] = getDigitalBarCodeCheckerDigit(barcodeArray);

        String output = "";
        for(int i = 0; i < 44; i++) {
            output += barcodeArray[i];
        }

        return output;
    }

    private int getMaturityFactor(java.util.Date expDate) {
        if(null != expDate) {
            String baseDateString = "03/07/2000"; // format: dd/MM/yyyy
            java.util.Date baseDate = null;
            try {
                baseDate = DATE_FORMAT_2.parse(baseDateString);
            } catch (ParseException e) {
                // wont happen, still for safety
                throw new RuntimeException("Server Error");
            }
            // assuming expdate will always be greater or equal to base date
            long diff = expDate.getTime() - baseDate.getTime();
            long numDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) % 1000;


            return (int) (numDays + 1000);
        }else {
            return 1000; // assuming base date is equal to expiry date// Dont know what to do, need to discuss
        }
    }

    private int getDigitalBarCodeCheckerDigit(int[] barcodeArray) {
        int[] multiplierArray = new int[44];
        int count = 2;
        for(int i = 43; i >= 0; i--) {
            if(i == 4) continue; //skip position 5
            multiplierArray[i] = count;
            count++;

            if(count == 10) {
                count = 2;
            }
        }

        int totalSum = 0;// maximum multiplication of single didgit is 81 and 81 times 44 comes under range of int
        for(int i = 0; i < 44; i++) {
            if(i == 4) continue; //skip position 5 [not required since we already made multiplierArry[4] = 0
            int a = barcodeArray[i];
            int b = multiplierArray[i];
            int bb = (a * b);
            totalSum += bb;
        }

        int rem11 = totalSum % 11;
        int finalDigit = 11 - rem11;
        if(finalDigit == 0 || finalDigit == 10 || finalDigit == 11) {
            finalDigit = 1;
        }
        return finalDigit;
    }


}
