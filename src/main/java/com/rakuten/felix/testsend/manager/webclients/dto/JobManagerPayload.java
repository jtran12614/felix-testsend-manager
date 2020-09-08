package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobManagerPayload {
    String namespace;
    Map<String, Object> info;
    Map<String, Object> replyHeader;
    String replyDestination;
    List<Object> workflow;
}
