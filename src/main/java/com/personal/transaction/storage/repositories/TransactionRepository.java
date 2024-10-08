package com.personal.transaction.storage.repositories;

import com.personal.transaction.storage.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("select t from Transaction t where transactionDate >= ?1 and transactionDate < ?2")
    List<Transaction> findAllByTransactionDate(Long startDate, Long endDate);
}
