package com.personal.transaction.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.transaction.storage.consumers.TransactionConsumer;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.repositories.TransactionRepository;
import com.personal.transaction.storage.service.TransactionStorageService;
import com.personal.transaction.storage.utils.TransactionTestUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
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

import java.nio.charset.StandardCharsets;
import java.util.*;


@Testcontainers
@EnableKafka
@ActiveProfiles(value = "test", profiles = {"test"})
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(
        partitions = 1,
        topics = { "${spring.kafka.topic}" },
        bootstrapServersProperty = "localhost:29092",
        brokerProperties = {
        "listeners=PLAINTEXT://localhost:29092",
        "port=29092"
        }
)
@SpringBootTest(classes = {TransactionStorageApplication.class, Transaction.class})
@DirtiesContext
public class SimpleKafkaTest {

    private static final String TEST_TOPIC = "TRANSACTION.TOPIC";

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.0"))
            .withFileSystemBind("db/migration/V1.0__Create_transaction_table.sql", "/docker-entrypoint-initdb.d", BindMode.READ_WRITE);

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

    private Producer<String, byte[]> producer;

    List<Transaction> transactionSampleList;

    private List<Transaction> retrievedTransactions;

    @BeforeEach
    public void setUp() {
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    embeddedKafkaBroker.getPartitionsPerTopic());
        }
        producer = configureProducer();
        transactionSampleList = new ArrayList<>();
        retrievedTransactions = new ArrayList<>();
    }

    @Test
    public void testReceivingValidTransactionEvents() throws InterruptedException {
        givenValidTransactionList();
        whenTransactionsProduced(producer);
        thenValidTransactionShouldBeStoredForValidTransaction();
    }

    @Test
    public void testReceivingInvalidNegativeAmountTransactionEvents() throws InterruptedException {
        givenInvalidNegativeAmountTransactionList();
        whenTransactionsProduced(producer);
        thenValidTransactionShouldBeStoredForNegativeTransactionAmount();
    }

    @Test
    public void testReceivingInvalidDescriptionTransactionEvents() throws InterruptedException {
        givenInvalidDescriptionTransactionList();
        whenTransactionsProduced(producer);
        thenValidTransactionShouldBeStoredForInvalidTransactionDescription();
    }

    private Producer<String, byte[]> configureProducer() {
        Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<String, byte[]>(producerProps).createProducer();
    }

    private void givenValidTransactionList() {
        transactionSampleList.add(Transaction.builder()
                .description(TransactionTestUtils.TEST_VALID_DESCRIPTION)
                .transactionDate(TransactionTestUtils.TEST_TRANSACTION_DATE)
                .amount(TransactionTestUtils.TEST_TRANSACTION_AMOUNT_2)
                .build());
    }

    private void givenInvalidNegativeAmountTransactionList() {
        transactionSampleList.add(TransactionTestUtils.TEST_TRANSACTION_WITH_NEGATIVE_AMOUNT_2);
    }

    private void givenInvalidDescriptionTransactionList() {
        transactionSampleList.add(TransactionTestUtils.TEST_TRANSACTION_WITH_INVALID_DESCRIPTION);
    }

    private void whenTransactionsProduced(Producer<String, byte[]> producer) throws InterruptedException {
        transactionSampleList.forEach(sampleTransaction -> {
            try {
                producer.send(
                        new ProducerRecord<>(
                                TEST_TOPIC,
                                "my-test-key",
                                objectMapper.writeValueAsString(sampleTransaction).getBytes(StandardCharsets.UTF_8)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(2000);
        producer.close();
    }

    private void thenValidTransactionShouldBeStoredForValidTransaction() {
        Transaction storedTransaction = retrieveTransaction();
        validateValidDescription(storedTransaction);
        validateValidTransactionDate(storedTransaction);
        validateValidTransactionAmount(storedTransaction);
    }

    private void thenValidTransactionShouldBeStoredForNegativeTransactionAmount() {
        Transaction storedTransaction = retrieveTransaction();
        validateValidDescription(storedTransaction);
        validateValidTransactionDate(storedTransaction);
        validateNegativeTransactionAmount(storedTransaction);
    }

    private void thenValidTransactionShouldBeStoredForInvalidTransactionDescription() {
        Transaction storedTransaction = retrieveTransaction();
        validateInvalidDescription(storedTransaction);
        validateValidTransactionDate(storedTransaction);
        validateValidTransactionAmount(storedTransaction);
    }

    private Transaction retrieveTransaction() {
        retrievedTransactions = transactionRepository.findAllByTransactionDate(
                TransactionTestUtils.TEST_TRANSACTION_DATE,
                TransactionTestUtils.TEST_TRANSACTION_DATE + 1000);
        Assertions.assertFalse(retrievedTransactions.isEmpty());
        Assertions.assertEquals(retrievedTransactions.size(), 1);
        Transaction storedTransaction = retrievedTransactions.getFirst();
        Assertions.assertNotNull(storedTransaction);
        return storedTransaction;
    }

    private static void validateValidDescription(Transaction storedTransaction) {
        Assertions.assertEquals(storedTransaction.getDescription(), TransactionTestUtils.TEST_VALID_DESCRIPTION);
    }

    private static void validateInvalidDescription(Transaction storedTransaction) {
        Assertions.assertTrue(storedTransaction.getDescription().isEmpty());
    }

    private static void validateValidTransactionDate(Transaction storedTransaction) {
        Assertions.assertEquals(storedTransaction.getTransactionDate(), TransactionTestUtils.TEST_TRANSACTION_DATE);
    }

    private static void validateValidTransactionAmount(Transaction storedTransaction) {
        Assertions.assertEquals(storedTransaction.getAmount(), TransactionTestUtils.TEST_TRANSACTION_AMOUNT_2);
    }

    private static void validateNegativeTransactionAmount(Transaction storedTransaction) {
        Assertions.assertEquals(storedTransaction.getAmount(), 0.0);
    }

    @AfterEach
    public void cleanup() {
        transactionRepository.deleteAll();
    }
}