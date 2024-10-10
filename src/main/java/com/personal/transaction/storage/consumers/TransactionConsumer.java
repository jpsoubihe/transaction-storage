package com.personal.transaction.storage.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.service.TransactionStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger("TRANSACTION_CONSUMER");

    @Autowired
    private TransactionStorageService transactionStorageService;

    @KafkaListener(
            id = "transaction-listener",
            topics = "transaction",
            clientIdPrefix = "transaction-listener",
            groupId = "transaction-listener",
            properties = "value.deserializer:com.personal.transaction.storage.model.TransactionDeserializer")
    public void listen(List<Transaction> consumedTransactions) {
        MDC.put("requestId", UUID.randomUUID().toString());
        LOGGER.info("Just received a transaction batch! {}", consumedTransactions);
        transactionStorageService.storeTransactions(consumedTransactions);
        MDC.clear();
    }
}
