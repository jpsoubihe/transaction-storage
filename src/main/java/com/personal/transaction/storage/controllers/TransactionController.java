package com.personal.transaction.storage.controllers;

import com.personal.transaction.storage.model.ConvertedTransactionResponse;
import com.personal.transaction.storage.model.Transaction;
import com.personal.transaction.storage.service.ExchangeService;
import com.personal.transaction.storage.service.TransactionStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

@RequestMapping("v1/transactions")
@RestController
public class TransactionController {

    TransactionStorageService transactionStorageService;

    ExchangeService exchangeService;

    public TransactionController(TransactionStorageService transactionStorageService, ExchangeService exchangeService) {
        this.transactionStorageService = transactionStorageService;
        this.exchangeService = exchangeService;
    }

    @GetMapping
    public ResponseEntity<List<ConvertedTransactionResponse>> getTransactions(
            @RequestParam(name = "exchange_currency") String exchangeCurrency,
            @RequestParam(name = "start_date") String startDate,
            @RequestParam(name = "end_date", required = false) String endDate
            ) throws ParseException {
        List<Transaction> retrievedTransactions = transactionStorageService.retrieveTransactions(startDate, endDate);
        if (retrievedTransactions.isEmpty()) {
            return ResponseEntity.ok(Collections.EMPTY_LIST);
        }
        List<ConvertedTransactionResponse> convertedTransactions =
                exchangeService.processCurrencyExchangeInfo(exchangeCurrency, startDate, retrievedTransactions);
        return ResponseEntity.ok(convertedTransactions);
    }

}
