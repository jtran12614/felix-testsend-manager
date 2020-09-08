package com.rakuten.felix.testsend.manager.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplyJobStatusPayload {

    private Long id;

    @NotNull
    private Map<String, Object> info;

    @NotNull
    private JobStatus status;

    @NotNull
    private Boolean success;

    private String message;
}
