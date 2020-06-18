package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

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
    Columns columns;
    Integer shopId;
    Integer newsId;

    public PermissionType getPermissionType() {
        if (Objects.nonNull(newsId)) {
            return PermissionType.EMAGAZINE;
        } else if (Objects.nonNull(shopId)) {
            return PermissionType.RMAIL;
        } else {
            return PermissionType.NONE;
        }
    }
}
