package com.rakuten.felix.testsend.manager.utils;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;

@Component
public class TimeUtils {

    public ZonedDateTime getCurrentTime(Clock clock) {
        return ZonedDateTime.now(clock);
    }
}