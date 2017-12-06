package com.rakuten.felix.testsend.manager.messaging.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Notification {
    Long userId;
    Boolean noticeFlag;
    String url;
    String title;
    String message;
}
