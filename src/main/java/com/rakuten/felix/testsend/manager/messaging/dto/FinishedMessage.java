package com.rakuten.felix.testsend.manager.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishedMessage {
    @NotNull
    @Min(0)
    Integer jobId;
}
