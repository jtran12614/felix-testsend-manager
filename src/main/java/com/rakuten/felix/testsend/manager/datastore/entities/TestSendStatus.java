package com.rakuten.felix.testsend.manager.datastore.entities;

import lombok.extern.log4j.Log4j;

import java.util.Arrays;

@Log4j
public enum TestSendStatus {
    NEW(0),
    FINISHED(1),
    ERROR(2);

    private final Integer number;

    TestSendStatus(Integer number) {
        this.number = number;
    }

    public Integer toNumber() {
        return number;
    }

    /**
     * Deserialization factory.
     *
     * @param number Status number.
     * @return Where type.
     */
    public static TestSendStatus fromNumber(Integer number) {
        return Arrays.stream(values())
                .filter(whereType -> whereType.number.equals(number))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Conversion failed for number: " + number);
                    return null;
                });
    }
}
