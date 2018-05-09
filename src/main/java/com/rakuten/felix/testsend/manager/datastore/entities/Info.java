package com.rakuten.felix.testsend.manager.datastore.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Info {
    List<String> subjects;
    List<String> htmlContents;
    List<String> textContents;
    String errorMessage;
    User user;
    List<String> recipients;
}
