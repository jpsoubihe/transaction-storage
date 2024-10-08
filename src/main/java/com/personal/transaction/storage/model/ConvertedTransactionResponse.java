package com.personal.transaction.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConvertedTransactionResponse {

    private String transactionId;

    private String transactionDate;

    private Double originalAmount;

    private Double exchangeRate;

    private Double convertedAmount;
}
