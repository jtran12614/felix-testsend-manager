package com.rakuten.felix.testsend.manager.webclients.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {
    @NotNull
    List<Subject> subjects;
    @NotNull
    List<Content> contents;
}
