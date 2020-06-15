package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalizerColumn {
    Integer elementId;
    Boolean skipNoProducts;
    Integer attributeIndex;
    String keyword;
    String replace;
}
