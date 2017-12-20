package com.rakuten.felix.testsend.manager.messaging.dto;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
public class FinishedMessage {
    @NotNull
    @Min(0)
    Integer jobId;
    @NotNull
    @Min(0)
    Integer scheduleId;
}
