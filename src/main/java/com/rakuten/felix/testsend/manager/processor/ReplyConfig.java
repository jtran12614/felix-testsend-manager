package com.rakuten.felix.testsend.manager.processor;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ReplyConfig {
    private final String jobStatusHandlingChannel;

    public ReplyConfig(
            @Value("${spring.cloud.stream.rabbit.bindings.in-job-manager-reply.consumer.prefix}") String jobStatusReplyPrefix,
            @Value("${spring.cloud.stream.bindings.in-job-manager-reply.destination}") String jobStatusReplyDestination) {

        this.jobStatusHandlingChannel = jobStatusReplyPrefix + jobStatusReplyDestination;
    }
}
