package com.personal.transaction.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.transaction.storage.consumers.TransactionConsumer;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.model.TransactionDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// ToDo: prove this is worthy and refactor for clarity
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = { "transaction" },bootstrapServersProperty = "localhost:9092")
@SpringBootTest
public class SimpleKafkaTest {

    private static final String TEST_TOPIC = "transaction";

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    TransactionConsumer transactionConsumer;

    @Autowired
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> kafkaListenerContainerFactory;

    @Test
    public void testReceivingKafkaEvents() throws IOException {
        Consumer<String, Transaction> consumer = configureConsumer();
        Producer<String, byte[]> producer = configureProducer();

        Transaction t = Transaction.builder()
                .description("Test transaction")
                .transactionDate(new Date())
                .amount(23.75)
                .build();
        String serializedTransaction = objectMapper.writeValueAsString(t);
        System.out.println("Object is " + serializedTransaction);
        System.out.println("Object is " + objectMapper.readValue(serializedTransaction, Transaction.class));


        producer.send(new ProducerRecord<>(TEST_TOPIC, "my-test-key", objectMapper.writeValueAsString(t).getBytes(StandardCharsets.UTF_8)));

        ConsumerRecord<String, Transaction> singleRecord = KafkaTestUtils.getSingleRecord(consumer, TEST_TOPIC);
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.key()).isEqualTo("my-test-key");
        assertThat(singleRecord.value()).isEqualTo(t);
        consumer.close();
        producer.close();
    }

    private Consumer<String, Transaction> configureConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("transaction-listener", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, TransactionDeserializer.class);
        Consumer<String, Transaction> consumer = new DefaultKafkaConsumerFactory<String, Transaction>(consumerProps)
                .createConsumer();
        consumer.subscribe(Collections.singleton(TEST_TOPIC));
        return consumer;
    }

    private Producer<String, byte[]> configureProducer() {
        Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<String, byte[]>(producerProps).createProducer();
    }
}