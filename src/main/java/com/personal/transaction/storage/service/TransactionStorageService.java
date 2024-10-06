package com.personal.transaction.storage.service;

import com.personal.transaction.storage.exceptions.InvalidDescriptionException;
import com.personal.transaction.storage.exceptions.InvalidTransactionAmountException;
import com.personal.transaction.storage.model.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStorageService.class);


    public List<Transaction> storeTransactions(List<Transaction> transactionList) {
        transactionList.forEach(this::processTransactions);
        // saveAll
        return transactionList;
    }

    private void processTransactions(Transaction transaction) {
        transaction.setTransactionId(UUID.randomUUID().toString());
        try {
            validateDescription(transaction.getDescription());
            validateTransactionAmount(transaction.getAmount());
        } catch (InvalidDescriptionException invalidDescriptionException) {
            LOGGER.error("Error on description validation. Storing transaction with a null description. {}",
                    invalidDescriptionException.getMessage());
            transaction.setDescription(null);
        } catch (InvalidTransactionAmountException invalidTransactionAmountException) {
            LOGGER.error("Error on transaction amount validation. Storing transaction with amount 0. {}",
                    invalidTransactionAmountException.getMessage());
            transaction.setAmount(0.00);
        }
    }

    private void validateDescription(String description) {
        if(StringUtils.isNotEmpty(description) && description.length() > 50) {
            throw new InvalidDescriptionException("Invalid description, size is larger than 50 chars.");
        }
    }

    private Instant validateDate(Instant transactionDate) {
        // check if transactionDate is on expected format
        // if so do nothing
        // if not, format as expected
        return transactionDate;
    }

    private Double validateTransactionAmount(Double transactionAmount) {
        if(transactionAmount == null || transactionAmount <= 0) {
            throw new InvalidTransactionAmountException(String.format("Transaction amount should have a valid positive value. Current value is {}.", transactionAmount));
        }

        // Guarantee a rounding on a cent level
        BigDecimal bd = new BigDecimal(transactionAmount).setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }
}
