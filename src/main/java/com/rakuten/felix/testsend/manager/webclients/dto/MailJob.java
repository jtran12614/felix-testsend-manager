package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailJob {
    @NotEmpty
    @Valid
    List<@NotNull Schedule> schedules;
    @NotNull
    List<String> parts;
    @NotNull
    List<String> prependAddresses;
}
