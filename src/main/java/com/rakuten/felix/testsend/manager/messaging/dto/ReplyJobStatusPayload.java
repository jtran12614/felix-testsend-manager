package com.rakuten.felix.testsend.manager.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rakuten.felix.testsend.manager.webclients.dto.Info;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplyJobStatusPayload {

    private Long id;

    @NotNull
    private Info info;

    @NotNull
    private JobStatus status;

    @NotNull
    private Boolean success;

    private String message;
}
