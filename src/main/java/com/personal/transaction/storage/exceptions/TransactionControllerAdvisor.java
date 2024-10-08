package com.personal.transaction.storage.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.ParseException;

@ControllerAdvice
public class TransactionControllerAdvisor {

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<String> parseException(ParseException parseException) {
        return ResponseEntity.badRequest().body("Date not on expected YYY-MM-DD hh-mm-ss format.");
    }
}
