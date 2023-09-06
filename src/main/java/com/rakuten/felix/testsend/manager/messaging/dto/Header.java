package com.rakuten.felix.testsend.manager.messaging.dto;

import com.rakuten.felix.common.web.security.service.WebSession;
import com.rakuten.felix.jobfacade.dto.core.JobStartPayload;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.rakuten.felix.testsend.manager.processor.Processor.REPLY_HEADERS;

@Slf4j
public class Header extends HashMap<String, Object> {
    private static final String LOG_ID_KEY = "LOG_ID";
    private static final String TEST_ID_KEY = "TEST_ID";
    private static final String REPLY_DESTINATION_KEY = "REPLY_DESTINATION";
    private static final String CLIENT_ID       = "Client-ID";
    private static final String ENV             = "X-Env";
    private static final String ENV_DEFAULT     = "blue";
    private static final String DEFAULT         = "X-Default";
    private static final String DEFAULT_VAL     = "true";
    private static final String USER_ID         = "User-ID";
    private static final String USER_NAME       = "Username";

    public static Header buildWithContentTypeV2(Map<String, Object> jobPayload, String logId, Integer testId, String replyDestination, WebSession session) {
        log.debug("ReplyDestination: {}", replyDestination);
        Header header = new Header();

        HashMap<String, Object> currentHeader = (HashMap<String, Object>) jobPayload.get(REPLY_HEADERS);
        if (currentHeader != null) {
            header.putAll(currentHeader);
        }
        header.put(CLIENT_ID, session.getClientId());
        header.put(USER_ID, session.getUserId());
        header.put(USER_NAME, session.getUserName());
        header.put(ENV, Optional.of(session)
                .map(WebSession::getEnvironment)
                .orElse(ENV_DEFAULT));
        header.put(LOG_ID_KEY, logId);
        header.put(TEST_ID_KEY, testId);
        header.put(REPLY_DESTINATION_KEY, replyDestination);
        header.put(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return header;
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
        header.put(ENV, ENV_DEFAULT);
        header.put(DEFAULT, DEFAULT_VAL);
        header.put(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return header;
    }

}
