package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailJob {
    @NotNull
    @Valid
    List<Schedule> schedules;
    @NotNull
    @NotEmpty
    List<String> parts;
    @NotNull
    List<String> prependAddresses;
}
