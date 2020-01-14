package com.rakuten.felix.testsend.manager.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

public interface InputChannels {
    String IN_TEST_SEND_FINISHED = "in-test-send-finished";
    String IN_TEST_SEND_ERROR = "in-test-send-error";
    String JOB_MANAGER_REPLY = "job-manager-reply";

    @Input(IN_TEST_SEND_FINISHED)
    MessageChannel inTestSendFinished();

    @Input(IN_TEST_SEND_ERROR)
    MessageChannel inTestSendError();

    @Input(JOB_MANAGER_REPLY)
    MessageChannel inJobManagerReply();
}
