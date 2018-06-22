package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterCampaignResponse {
    @NotNull
    @Min(1)
    Integer jdkId;
}
