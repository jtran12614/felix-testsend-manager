package com.rakuten.felix.testsend.manager.datastore;

public class HistoryNotFoundException extends RuntimeException {
    public HistoryNotFoundException(String message, Integer id) {
        super(String.format("History not found: %s: id=%d", message, id));
    }
}
