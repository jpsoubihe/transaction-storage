package com.personal.transaction.storage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Transaction implements Serializable {

    @Id
    private String transactionId;

    private String description;

    private Double amount;

    private Long transactionDate;
}
