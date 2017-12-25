package com.rakuten.felix.testsend.manager.datastore.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@AllArgsConstructor
public enum TestSendStatus {
    NEW(0),
    FINISHED(1),
    ERROR(2);

    private final Integer number;

    /**
     * Deserialization factory.
     *
     * @param number Status number.
     * @return Where type.
     */
    @JsonValue
    public static TestSendStatus fromNumber(Integer number) {
        return Arrays.stream(values())
                .filter(whereType -> whereType.number.equals(number))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Conversion failed for number: " + number);
                    return null;
                });
    }

    @JsonCreator
    public Integer toNumber() {
        return number;
    }
}
