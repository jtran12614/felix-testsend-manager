package com.rakuten.felix.testsend.manager.web.dto;

import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.Value;
import org.json.simple.JSONObject;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
public class KickTestSendRequest {
    @NotNull
    @Min(0)
    Integer bundleId;
    @NotNull
    @Min(0)
    Integer bundleType;
    @NotNull
    JSONObject job;
    User user;
}
