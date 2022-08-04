package com.rakuten.felix.testsend.manager.webclients.workflow.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JobStartResponse {
    Long jobId;
    String message;
}
