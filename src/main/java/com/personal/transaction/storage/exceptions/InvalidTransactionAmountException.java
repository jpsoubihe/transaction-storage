package com.personal.transaction.storage.exceptions;

public class InvalidTransactionAmountException extends RuntimeException {

    public InvalidTransactionAmountException(String message) {
        super(message);
    }
}
