package com.rakuten.felix.testsend.manager.datastore;

public class HistoryNotFoundException extends RuntimeException {
    public HistoryNotFoundException(Integer id) {
        super("History not found id=" + id);
    }
}
