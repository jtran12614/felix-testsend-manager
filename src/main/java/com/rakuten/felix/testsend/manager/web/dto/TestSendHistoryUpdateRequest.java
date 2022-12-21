package com.rakuten.felix.testsend.manager.web.dto;

import com.rakuten.felix.jobmanager.dto.core.JobStatus;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Value
@Builder(toBuilder = true)
public class TestSendHistoryUpdateRequest {
    String errorMessage;

    JobStatus jobStatus;
}
