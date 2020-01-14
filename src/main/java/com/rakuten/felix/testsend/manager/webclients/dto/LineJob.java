package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineJob {
    Info info;
    Map<String, Object> replyHeader;
    String replyDestination;
    List<Object> workflow;
}
