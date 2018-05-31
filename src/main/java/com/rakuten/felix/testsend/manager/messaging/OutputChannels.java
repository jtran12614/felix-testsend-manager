package com.rakuten.felix.testsend.manager.messaging;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface OutputChannels {
    String OUT_ERROR = "out-error";
    String OUT_PUBLISH_NOTIFICATION = "out-publish-notification";

    @Output(OUT_ERROR)
    MessageChannel outError();

    @Output(OUT_PUBLISH_NOTIFICATION)
    MessageChannel outPublishNotification();
}
