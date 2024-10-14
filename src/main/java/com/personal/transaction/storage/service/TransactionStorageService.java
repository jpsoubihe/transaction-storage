package com.personal.transaction.storage.service;

import com.personal.transaction.storage.exceptions.InvalidDescriptionException;
import com.personal.transaction.storage.exceptions.InvalidTransactionAmountException;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.repositories.TransactionRepository;
import com.personal.transaction.storage.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger("TRANSACTION_CONSUMER");

    @Autowired
    private TransactionRepository transactionRepository;


    public List<Transaction> storeTransactions(List<Transaction> transactionList) {
        List<Transaction> validatedTransactions = transactionList.stream().map(this::processTransactions).collect(Collectors.toList());
        List<Transaction> persistedTransactions = null;
        try {
            LOGGER.info("Saving {} transaction in DB.", transactionList.size());
            persistedTransactions = transactionRepository.saveAll(validatedTransactions);
        } catch (Exception ex) {
            LOGGER.error("Error saving transactions in DB.", ex);
            // ToDo: handle exception
        }

        return Optional.ofNullable(persistedTransactions).orElse(List.of());
    }

    public List<Transaction> retrieveTransactions(String startDate, String endDate) throws ParseException {
        Long startDateMillis = DateUtils.validateDate(startDate);
        Long endDateMillis;

        if (StringUtils.isNotEmpty(endDate)) {
            endDateMillis = DateUtils.validateDate(endDate);
        } else {
            endDateMillis = Instant.now().toEpochMilli();
        }

        return transactionRepository.findAllByTransactionDate(startDateMillis, endDateMillis);
    }

    private Transaction processTransactions(Transaction transaction) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .description(validateDescription(transaction.getDescription()))
                .amount(validateTransactionAmount(transaction.getAmount()))
                .transactionDate(Optional.ofNullable(transaction.getTransactionDate())
                        .orElse(Instant.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()))
                .build();
    }

    private String validateDescription(String description) {

        if(StringUtils.isNotEmpty(description) && description.length() > 50) {
//            throw new InvalidDescriptionException("Invalid description, size is larger than 50 chars.");
            LOGGER.error("Invalid description, size is larger than 50 chars.");
            return StringUtils.EMPTY;
        }
        return description;
    }

    private double validateTransactionAmount(Double transactionAmount) {
        if(transactionAmount == null || transactionAmount <= 0) {
           LOGGER.error("Error on transaction amount validation. " +
                   "Transaction amount should have a valid positive value. Current value is {}. " +
                   "Storing transaction with amount 0.", transactionAmount);
           return 0.0;
        }

        // Guarantee a rounding on a cent level
        BigDecimal bd = new BigDecimal(transactionAmount).setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }
}
