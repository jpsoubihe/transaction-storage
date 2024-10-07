package com.personal.transaction.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class TransactionStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionStorageApplication.class, args);
	}

}
