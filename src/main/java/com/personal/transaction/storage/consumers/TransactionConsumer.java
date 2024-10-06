package com.personal.transaction.storage.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.transaction.storage.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionConsumer.class);

    @KafkaListener(
            id = "transaction-listener",
            topics = "transaction",
            clientIdPrefix = "transaction-listener",
            groupId = "transaction-listener",
            properties = "value.deserializer:com.personal.transaction.storage.model.TransactionDeserializer")
    public void listen(List<Transaction> consumedTransactions) {
        LOGGER.info("Just received a transaction batch! {}", consumedTransactions);
    }
}
