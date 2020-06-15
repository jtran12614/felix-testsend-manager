package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampDeserializer;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampSerializer;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {
    Integer id;
    List<Integer> deviceCodes;
    @JsonDeserialize(using = UnixTimeStampDeserializer.class)
    @JsonSerialize(using = UnixTimeStampSerializer.class)
    @NotNull
    ZonedDateTime reserveDate;
    @NotNull
    List<Subject> subjects;
    @NotNull
    List<Content> contents;
}
