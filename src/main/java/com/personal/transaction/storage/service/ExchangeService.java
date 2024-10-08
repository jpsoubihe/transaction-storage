package com.personal.transaction.storage.service;

import com.personal.transaction.storage.client.TreasuryDatasetClient;
import com.personal.transaction.storage.model.ConvertedTransactionResponse;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class ExchangeService {

    TreasuryDatasetClient treasuryDatasetClient;

    public ExchangeService(TreasuryDatasetClient treasuryDatasetClient) {
        this.treasuryDatasetClient = treasuryDatasetClient;
    }

    public List<ConvertedTransactionResponse> processCurrencyExchangeInfo(String exchangeCurrency, String startDate, List<Transaction> transactionList) throws ParseException {
        String recordDate = prepareRecordMinDate(startDate);

        treasuryDatasetClient.getExchangeRate();

        // process response

        // convert amount

       return transactionList.stream()
               .map(transaction ->
                       ConvertedTransactionResponse.builder()
                               .transactionId(transaction.getTransactionId())
                               .transactionDate(prepareResponseDate(transaction.getTransactionDate()))
                               .originalAmount(transaction.getAmount())
                               .build())
               .toList();
    }

    private String prepareRecordMinDate(String date) throws ParseException {
        Long dateMillis = DateUtils.validateDate(date);
        return DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneId.of("UTC"))
                .format(Instant.ofEpochMilli(dateMillis).minus(180, ChronoUnit.DAYS));

    }

    private String prepareResponseDate(Long date) {
        return DateUtils.validDateFormat.format(new Date(date));
    }
}
