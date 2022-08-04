package com.rakuten.felix.testsend.manager.webclients.testrecipient.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder
public class TestRecipient {
    @NotNull
    private String recipientAddress;
}
