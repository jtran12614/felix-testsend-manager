package com.rakuten.felix.testsend.manager.messaging.dto;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;

@Slf4j
public class Header extends HashMap<String, Object> {
    private static final String LOG_ID_KEY = "LOG_ID";
    private static final String TEST_ID_KEY = "TEST_ID";
    private static final String REPLY_DESTINATION_KEY = "REPLY_DESTINATION";

    private static final String NAMESPACE = "namespace";
    private static final String NAMESPACE_LINE = "LINE";

    public Header() {
        this.putIfAbsent(NAMESPACE, NAMESPACE_LINE);
    }

    public String getLogId() {
        return (String) this.get(LOG_ID_KEY);
    }

    public Integer getTestId() {
        return (Integer) this.get(TEST_ID_KEY);
    }

    public static Header buildNoReply(String logId) {
        val header = new Header();
        header.put(LOG_ID_KEY, logId);
        return header;
    }

    public static Header buildWithContentType(String logId, Integer testId, String replyDestination) {
        log.debug("ReplyDestination: {}", replyDestination);
        val header = new Header();
        header.put(LOG_ID_KEY, logId);
        header.put(TEST_ID_KEY, testId);
        header.put(REPLY_DESTINATION_KEY, replyDestination);
        header.put(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return header;
    }

    public static Header buildWithContentType(String logId) {
        val header = new Header();
        header.put(LOG_ID_KEY, logId);
        header.put(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return header;
    }
}
