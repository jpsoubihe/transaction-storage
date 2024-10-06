package com.personal.transaction.storage.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class TransactionDeserializer implements Deserializer<Transaction>, Serializer<Transaction> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDeserializer.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public Transaction deserialize(String s, byte[] bytes) {
        LOGGER.info("Deserializing transaction");
        Transaction t = null;
        try {
            t = OBJECT_MAPPER.readValue(bytes, Transaction.class);
        } catch (Exception e) {
            LOGGER.error("Problems when deserializing consumed transaction.");
            throw new RuntimeException(e);
        }
//        t.setTransactionId(UUID.randomUUID().toString());
        return t;
    }

    @Override
    public byte[] serialize(String s, Transaction transaction) {
        try {
            return OBJECT_MAPPER.writeValueAsString(transaction).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Problems when serializing transaction {}", transaction);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
