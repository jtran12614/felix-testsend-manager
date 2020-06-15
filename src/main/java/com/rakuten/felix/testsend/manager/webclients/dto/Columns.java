package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Columns {
    Column email;
    Column name;
    Column easyId;
    Column userId;
    Integer deviceCode;
    List<Column> additional;
    List<PersonalizerColumn> personalizer;
}
