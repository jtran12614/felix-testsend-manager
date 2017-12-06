package com.rakuten.felix.testsend.manager.processor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class Info {
    List<String> subjects;
    List<String> htmlContents;
    List<String> textContents;
    String errorMessage;
    User user;
}
