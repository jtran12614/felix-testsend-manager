package com.rakuten.felix.testsend.manager.web.dto;

import com.rakuten.felix.testsend.manager.datastore.entities.Info;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Value
@Builder(toBuilder = true)
public class TestSendHistoryInitializeRequest {
    @Positive
    Integer jobId;

    @NotNull
    @PositiveOrZero
    Integer bundleId;

    // TODO: create common enum and add reference to it
    @NotNull
    @PositiveOrZero
    Integer bundleType;

    // data for monitoring and debugging only
    @NotNull
    Info info;
}
