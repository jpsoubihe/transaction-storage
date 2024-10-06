package com.personal.transaction.storage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("v1/transactions")
@RestController
public class TestController {

    @PostMapping
    public ResponseEntity<Void> saveTransaction() {
        return ResponseEntity.ok().build();
    }
}
