package com.personal.transaction.storage.utils;

import com.personal.transaction.storage.model.Transaction;

import java.time.Instant;

public class TransactionTestUtils {

    public static final Long TEST_TRANSACTION_DATE = 1728349215L;

    public static final String TEST_VALID_DESCRIPTION = "Test Description";

    public static final String TEST_INVALID_DESCRIPTION =
            "Test Description with more than 50 chars to break everything";

    public static final double TEST_TRANSACTION_DEFAULT_AMOUNT = 0.00;

    public static final double TEST_TRANSACTION_AMOUNT_1 = 24.5674;

    public static final double TEST_TRANSACTION_AMOUNT_2 = 24.56;

    public static final int DESCRIPTION_MAX_LENGTH = 50;

    public static final Transaction TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_1 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(-TEST_TRANSACTION_AMOUNT_1)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_2 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(-TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_POSITIVE_AMOUNT_1 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(TEST_TRANSACTION_AMOUNT_1)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_POSITIVE_AMOUNT_2 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_NULL_AMOUNT =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_NULL_DESCRIPTION =
            Transaction.builder()
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_INVALID_DESCRIPTION =
            Transaction.builder()
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .description(TEST_INVALID_DESCRIPTION)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_STORED_COMPLETE_TRANSACTION =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_STORED_CORRECTED_AMOUNT_TRANSACTION =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(TEST_TRANSACTION_DEFAULT_AMOUNT)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();

    public static final Transaction TEST_STORED_NO_DESCRIPTION_TRANSACTION =
            Transaction.builder()
                    .amount(TEST_TRANSACTION_AMOUNT_1)
                    .transactionDate(TEST_TRANSACTION_DATE)
                    .build();
}
