package com.rakuten.felix.testsend.manager.messaging.dto;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class ErrorMessage {
    @NotNull
    Integer jobId;
    @NotNull
    String errorMessage;
}
