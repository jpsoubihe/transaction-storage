package com.personal.transaction.storage.service;

import com.personal.transaction.storage.client.TreasuryDatasetClient;
import com.personal.transaction.storage.exceptions.CurrencyExchangeNotAvailableException;
import com.personal.transaction.storage.model.ConvertedTransactionResponse;
import com.personal.transaction.storage.model.ExchangeCurrencyDto;
import com.personal.transaction.storage.model.ExchangeRate;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        Double numericRate = getExchangeRateForCurrency(exchangeCurrency, recordDate);

        return transactionList.stream()
               .map(transaction ->
                       ConvertedTransactionResponse.builder()
                               .transactionId(transaction.getTransactionId())
                               .transactionDate(prepareResponseDate(transaction.getTransactionDate()))
                               .originalAmount(transaction.getAmount())
                               .convertedAmount(
                                       new BigDecimal(numericRate * transaction.getAmount())
                                               .setScale(2, RoundingMode.HALF_UP)
                                               .doubleValue())
                               .exchangeRate(numericRate)
                               .build())
               .toList();
    }

    private String prepareRecordMinDate(String date) throws ParseException {
        Long dateMillis = DateUtils.validateDate(date);
        return DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneId.of("UTC"))
                .format(Instant.ofEpochMilli(dateMillis).minus(180, ChronoUnit.DAYS));

    }

    private Double getExchangeRateForCurrency(String exchangeCurrency, String recordDate) {
        ExchangeRate exchangeRates = treasuryDatasetClient.getExchangeRate(recordDate, exchangeCurrency);

        ExchangeCurrencyDto rate = exchangeRates.getData().stream().findFirst()
                .orElseThrow(() -> new CurrencyExchangeNotAvailableException(
                        404,
                        String.format("No recent exchange currency found for %s.", exchangeCurrency)));

        return Double.valueOf(rate.getExchangeRate());
    }

    private String prepareResponseDate(Long date) {
        return DateUtils.validDateFormat.format(new Date(date));
    }
}
