package com.personal.transaction.storage.service;

import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.repositories.TransactionRepository;
import com.personal.transaction.storage.utils.TransactionTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TransactionStorageServiceTest {

    @InjectMocks
    TransactionStorageService transactionStorageService = new TransactionStorageService();

    @Mock
    TransactionRepository transactionRepository;

    List<Transaction> transactionList;

    List<Transaction> storedTransactions;

    @BeforeEach
    void setup() {
      transactionList = null;
      storedTransactions = null;
    }

    @Test
    void shouldRoundDoublePrecisionAmountToCents() {
        double amount = 23.45678;
        BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        Assertions.assertEquals(23.46, bd.doubleValue());
    }

    @Test
    void shouldShowIntegerAmountWithDoubleDecimalPrecision() {
        double amount = 23;
        BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        Assertions.assertEquals(23.00, bd.doubleValue());
    }

    @Test
    void shouldShowNegativeIntegerAmountWithDoubleDecimalPrecision() {
        double amount = -23.322345;
        BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        Assertions.assertEquals(-23.32, bd.doubleValue());
    }

    @Test
    void formatDateWithMillis() {
        Instant now = Instant.now();
        System.out.println("Current timestamp: " + now);
    }

    @Test
    void testTransactionWithNegativeAmountAndMoreThan2Decimals() {
        givenTransactionWithNegativeAmountAndMoreThan2Decimal();
        whenStoringTransactionsWithInvalidAmount();
        thenShouldHaveOnlyValidatedTransactions();
    }

    @Test
    void testTransactionWithNegativeAmountAndLessThan2Decimals() {
        givenTransactionWithNegativeAmountAndLessThan2Decimal();
        whenStoringTransactionsWithInvalidAmount();
        thenShouldHaveOnlyValidatedTransactions();
    }

    @Test
    void testTransactionWithNullAmount() {
        givenTransactionWithNullAmount();
        whenStoringTransactionsWithInvalidAmount();
        thenShouldHaveOnlyValidatedTransactions();
    }

    @Test
    void testTransactionWithPositiveAmountAndMoreThan2Decimals() {
        givenTransactionWithPositiveAmountAndMoreThan2Decimal();
        whenStoringCorrectTransactions();
        thenShouldHaveOnlyValidatedTransactions();
    }

    @Test
    void testTransactionWithPositiveAmountAndLessThan2Decimals() {
        givenTransactionWithPositiveAmountAndLessThan2Decimal();
        whenStoringCorrectTransactions();
        thenShouldHaveOnlyValidatedTransactions();
    }

    @Test
    void testTransactionWithNullDescription() {
        givenTransactionWithNullDescription();
        whenStoringTransactionsWithInvalidDescriptions();
        thenShouldHaveOnlyValidatedTransactions();
    }

    @Test
    void testTransactionWithMoreThan50CharsDescription() {
        givenTransactionWithInvalidDescription();
        whenStoringTransactionsWithInvalidDescriptions();
        thenShouldHaveOnlyValidatedTransactions();
    }

    void givenTransactionWithNegativeAmountAndMoreThan2Decimal() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_1);
    }

    void givenTransactionWithNegativeAmountAndLessThan2Decimal() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_2);
    }

    void givenTransactionWithPositiveAmountAndMoreThan2Decimal() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_POSITIVE_AMOUNT_1);
    }

    void givenTransactionWithPositiveAmountAndLessThan2Decimal() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_POSITIVE_AMOUNT_2);
    }

    void givenTransactionWithNullAmount() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_NULL_AMOUNT);
    }

    void givenTransactionWithNullDescription() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_NULL_DESCRIPTION);
    }

    void givenTransactionWithInvalidDescription() {
        transactionList = List.of(TransactionTestUtils.TEST_TRANSACTION_WITH_INVALID_DESCRIPTION);
    }

    void whenStoringCorrectTransactions() {
        Mockito.when(transactionRepository.saveAll(transactionList))
                .thenReturn(List.of(TransactionTestUtils.TEST_STORED_COMPLETE_TRANSACTION));
        storedTransactions = transactionStorageService.storeTransactions(transactionList);
    }

    void whenStoringTransactionsWithInvalidDescriptions() {
        Mockito.when(transactionRepository.saveAll(transactionList))
                .thenReturn(List.of(TransactionTestUtils.TEST_STORED_NO_DESCRIPTION_TRANSACTION));
        storedTransactions = transactionStorageService.storeTransactions(transactionList);
    }

    void whenStoringTransactionsWithInvalidAmount() {
        Mockito.when(transactionRepository.saveAll(transactionList))
                .thenReturn(List.of(TransactionTestUtils.TEST_STORED_CORRECTED_AMOUNT_TRANSACTION));
        storedTransactions = transactionStorageService.storeTransactions(transactionList);
    }

    void thenShouldHaveOnlyValidatedTransactions() {
        shouldStoreTransactions();
        shouldStoreTransactionsWithPositiveAmount();
        shouldStoreDescriptionWithMaximum50Chars();
    }

    void shouldStoreTransactions() {
        Assertions.assertNotNull(storedTransactions);
        Assertions.assertFalse(CollectionUtils.isEmpty(storedTransactions));
    }

    void shouldStoreTransactionsWithPositiveAmount() {
        storedTransactions.forEach(transaction -> Assertions.assertTrue(transaction.getAmount() >= 0));
        //Todo: Assert cents rounding
    }

    void shouldStoreDescriptionWithMaximum50Chars() {
        storedTransactions.forEach(validatedTransaction -> {
            Optional.ofNullable(validatedTransaction.getDescription())
                    .ifPresent(description ->
                            Assertions.assertTrue(description.length() <= TransactionTestUtils.DESCRIPTION_MAX_LENGTH));
        });
    }



}