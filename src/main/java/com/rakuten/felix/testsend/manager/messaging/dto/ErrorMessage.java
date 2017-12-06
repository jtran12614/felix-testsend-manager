package com.rakuten.felix.testsend.manager.messaging.dto;

import lombok.Value;

@Value
public class ErrorMessage {
    Integer jobId;
    String message;
}
