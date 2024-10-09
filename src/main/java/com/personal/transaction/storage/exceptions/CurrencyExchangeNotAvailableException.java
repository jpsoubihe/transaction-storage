package com.personal.transaction.storage.exceptions;

public class CurrencyExchangeNotAvailableException extends RuntimeException {

    int status;

    public CurrencyExchangeNotAvailableException(int status, String message) {
        super(message);
        this.status = status;
    }
}
