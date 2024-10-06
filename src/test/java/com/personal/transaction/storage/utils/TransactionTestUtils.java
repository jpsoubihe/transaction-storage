package com.personal.transaction.storage.utils;

import com.personal.transaction.storage.model.Transaction;

import java.time.Instant;

public class TransactionTestUtils {

    public static final String TEST_VALID_DESCRIPTION = "Test Description";

    public static final String TEST_INVALID_DESCRIPTION =
            "Test Description with more than 50 chars to break everything";

    public static final double TEST_TRANSACTION_AMOUNT_1 = 24.5674;

    public static final double TEST_TRANSACTION_AMOUNT_2 = 2.3;

    public static final Transaction TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_1 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(-TEST_TRANSACTION_AMOUNT_1)
                    .transactionDate(Instant.now())
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_2 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(-TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(Instant.now())
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_POSITIVE_AMOUNT_1 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(TEST_TRANSACTION_AMOUNT_1)
                    .transactionDate(Instant.now())
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_POSITIVE_AMOUNT_2 =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(Instant.now())
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_NULL_AMOUNT =
            Transaction.builder()
                    .description(TEST_VALID_DESCRIPTION)
                    .transactionDate(Instant.now())
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_NULL_DESCRIPTION =
            Transaction.builder()
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .transactionDate(Instant.now())
                    .build();

    public static final Transaction TEST_TRANSACTION_WITH_INVALID_DESCRIPTION =
            Transaction.builder()
                    .amount(TEST_TRANSACTION_AMOUNT_2)
                    .description(TEST_INVALID_DESCRIPTION)
                    .transactionDate(Instant.now())
                    .build();

    public static final int DESCRIPTION_MAX_LENGTH = 50;
}
