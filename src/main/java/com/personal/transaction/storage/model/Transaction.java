package com.personal.transaction.storage.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Transaction implements Serializable {

    String transactionId;

    String description;

    Double amount;

    Date transactionDate;

    Date createdAt;
}
