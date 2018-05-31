package com.rakuten.felix.testsend.manager.webclients.dto;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
public class RegisterCampaignResponse {
    @NotNull
    @Min(1)
    Integer jdkId;
}
