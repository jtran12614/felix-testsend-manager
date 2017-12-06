package com.rakuten.felix.testsend.manager.messaging;


import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

public interface InputChannels {
    String IN_KICK_TEST_SEND_FINISHED = "in-kick-test-send-finished";
    String IN_TEST_SEND_FINISHED = "in-test-send-finished";
    String IN_TEST_SEND_ERROR = "in-test-send-error";

    @Input(IN_KICK_TEST_SEND_FINISHED)
    MessageChannel inKickTestSendFinished();

    @Input(IN_TEST_SEND_FINISHED)
    MessageChannel inTestSendFinished();

    @Input(IN_TEST_SEND_ERROR)
    MessageChannel inTestSendError();
}
