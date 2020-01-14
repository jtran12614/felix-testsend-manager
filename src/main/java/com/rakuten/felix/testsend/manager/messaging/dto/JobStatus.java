package com.rakuten.felix.testsend.manager.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@AllArgsConstructor
public enum JobStatus {
    CREATED(0),
    PROCESSING(1),
    FINISHED(2),
    STOP_PROCESSING(3),
    STOPPED(3),
    PAUSED(4),
    FAILED(5),
    STOP_FAILED(6),
    DELETED(7);

    private final Integer value;

    @JsonValue
    public Integer getValue() {
        return value;
    }

    @JsonCreator
    public static JobStatus of(Integer value) {
        return Arrays.stream(values())
                     .filter(it -> it.value.equals(value))
                     .findFirst()
                     .orElse(null);
    }

    public String toString() {
        return getDeclaringClass().getSimpleName() + "." + name() + "(" + value + ")";
    }
}
