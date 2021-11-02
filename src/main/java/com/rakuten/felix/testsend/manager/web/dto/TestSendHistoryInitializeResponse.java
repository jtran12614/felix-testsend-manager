package com.rakuten.felix.testsend.manager.web.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Value
@Builder(toBuilder = true)
public class TestSendHistoryInitializeResponse {
    @NotNull
    @Positive
    Integer testSendHistoryId;
}
