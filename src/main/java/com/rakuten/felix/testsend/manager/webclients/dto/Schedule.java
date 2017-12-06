package com.rakuten.felix.testsend.manager.webclients.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
class Schedule {
    private List<Subject> subjects;
    private List<Content> contents;
}
