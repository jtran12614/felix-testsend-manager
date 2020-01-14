package com.rakuten.felix.testsend.manager;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class LoggingHelper {
    private static final String LOG_ID = "LOG_ID";

    public void setLogId(String logId) {
        MDC.put(LOG_ID, logId);
    }

    public void clear() {
        MDC.clear();
    }
}
