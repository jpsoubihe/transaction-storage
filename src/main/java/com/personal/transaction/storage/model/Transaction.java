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
    String transactionId;

    String description;

    Double amount;

    Instant transactionDate;
}
