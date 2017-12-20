package com.rakuten.felix.testsend.manager.messaging.dto;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
public class KickedMessage {
    @NotNull
    @Min(0)
    Integer id;
    Integer jobId;
}
