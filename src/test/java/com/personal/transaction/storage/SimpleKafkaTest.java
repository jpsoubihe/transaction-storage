package com.personal.transaction.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.transaction.storage.consumers.TransactionConsumer;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.model.TransactionDeserializer;
import com.personal.transaction.storage.repositories.TransactionRepository;
import com.personal.transaction.storage.service.TransactionStorageService;
import com.personal.transaction.storage.utils.TransactionTestUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Testcontainers
@EnableKafka
@ActiveProfiles(value = "test", profiles = {"test"})
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(
        partitions = 1,
        topics = { "transaction" },
        bootstrapServersProperty = "localhost:29092",
        brokerProperties = {
        "listeners=PLAINTEXT://localhost:29092",
        "port=29092"
        }
)
@SpringBootTest(classes = {TransactionStorageApplication.class, Transaction.class})
@DirtiesContext
public class SimpleKafkaTest {

    private static final String TEST_TOPIC = "transaction";

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.0"))
            .withFileSystemBind("db/migration/V1.0__Create_transaction_table.sql", "/docker-entrypoint-initdb.d", BindMode.READ_WRITE);
//            .withDatabaseName("transaction_db")
//            .withAccessToHost(true)
//            .withPassword("password")
//            .withUsername("transaction_user");
//            .withExposedPorts(5432);
//            .setPortBindings()
//            .withInitScript("db/migration/V1.0__Create_transaction_table.sql");

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    TransactionStorageService transactionStorageService;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    TransactionConsumer transactionConsumer;

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> kafkaListenerContainerFactory;

    @BeforeEach
    public void setUp() {
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    embeddedKafkaBroker.getPartitionsPerTopic());
        }
        System.out.println("Test query string = " + postgres.getPortBindings());
    }

    @Test
    public void testReceivingKafkaEvents() throws IOException, InterruptedException {
        Producer<String, byte[]> producer = configureProducer();

        Transaction t = Transaction.builder()
                .description(TransactionTestUtils.TEST_VALID_DESCRIPTION)
                .transactionDate(TransactionTestUtils.TEST_TRANSACTION_DATE)
                .amount(TransactionTestUtils.TEST_TRANSACTION_AMOUNT_2)
                .build();

        producer.send(new ProducerRecord<>(TEST_TOPIC, "my-test-key", objectMapper.writeValueAsString(t).getBytes(StandardCharsets.UTF_8)));

        Thread.sleep(2000);
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