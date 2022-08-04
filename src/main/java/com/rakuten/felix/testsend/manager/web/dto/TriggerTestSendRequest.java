package com.rakuten.felix.testsend.manager.web.dto;

import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Value
public class TriggerTestSendRequest {

    @Min(0)
    Integer bundleId;

    @NotNull
    @Min(0)
    Integer bundleType;

    @NotNull
    Map<String, Object> job;

    @NotNull
    User user;

    @NotEmpty
    Map<String, Object> contents;

    @NotNull
    @Valid
    RecipientData recipientData;
}
