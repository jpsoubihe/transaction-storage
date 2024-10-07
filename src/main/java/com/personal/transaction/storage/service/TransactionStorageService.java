package com.personal.transaction.storage.service;

import com.personal.transaction.storage.exceptions.InvalidDescriptionException;
import com.personal.transaction.storage.exceptions.InvalidTransactionAmountException;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.repositories.TransactionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStorageService.class);

    @Autowired
    private TransactionRepository transactionRepository;


    public List<Transaction> storeTransactions(List<Transaction> transactionList) {
        transactionList.stream().map(this::processTransactions).collect(Collectors.toList());
        List<Transaction> persistedTransactions = null;
        try {
            persistedTransactions = transactionRepository.saveAll(transactionList);
        } catch (Exception ex) {
            LOGGER.error("Error saving transactions in DB.", ex);
            // ToDo: handle exception
        }

        return Optional.ofNullable(persistedTransactions).orElse(List.of());
    }

    private Transaction processTransactions(Transaction transaction) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .description(validateDescription(transaction.getDescription()))
                .amount(validateTransactionAmount(transaction.getAmount()))
                .transactionDate(validateDate(transaction.getTransactionDate()))
                .build();
    }

    private String validateDescription(String description) {

        if(StringUtils.isNotEmpty(description) && description.length() > 50) {
//            throw new InvalidDescriptionException("Invalid description, size is larger than 50 chars.");
            LOGGER.error("Invalid description, size is larger than 50 chars.");
            return null;
        }
        return description;
    }

    private String validateDate(String transactionDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("UTC"));

        try {
            dateTimeFormatter.format(ZonedDateTime.parse(transactionDate));
        } catch (Exception e) {
            LOGGER.error("Wrong date in transaction payload {} storing current time in place.", transactionDate);
            return dateTimeFormatter.format(Instant.now());
        }
        return transactionDate;
    }

    private double validateTransactionAmount(Double transactionAmount) {
        if(transactionAmount == null || transactionAmount <= 0) {
//            throw new InvalidTransactionAmountException(String.format("Transaction amount should have a valid positive value. Current value is {}.", transactionAmount));
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
