package com.rakuten.felix.testsend.manager.web.dto;

import com.rakuten.felix.testsend.manager.webclients.dto.User;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Value
public class KickTestSendRequest {
    @NotNull
    @Min(0)
    Integer bundleId;

    @NotNull
    @Min(0)
    Integer bundleType;

    @NotNull
    Map<String, Object> job;

    User user;

    Object contents;

    List<String> recipients;
}
