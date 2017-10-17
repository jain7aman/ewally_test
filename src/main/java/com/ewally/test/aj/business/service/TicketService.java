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
import java.util.concurrent.TimeUnit;

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

        Date date = this.createDateFromDateString(rawTicket.getExpDate());
        String barcode = generateBarCode(rawTicket.getCode(), rawTicket.getValue(), date);
        java.sql.Date sqlDate = null;
        if(null != date) {
            sqlDate = new java.sql.Date(date.getTime());
        }
        this.ticketRepository.save(new Ticket(rawTicket.getCode(), rawTicket.getValue(), sqlDate, barcode, ticketType));
    }

    private String generateBarCode(String ticketCode, String value, Date expDate) {
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

    private int getMaturityFactor(Date expDate) {
        if(null != expDate) {
            String baseDateString = "03/07/2000"; // format: dd/MM/yyyy
            Date baseDate = null;
            try {
                baseDate = DATE_FORMAT.parse(baseDateString);
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
