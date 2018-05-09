package com.rakuten.felix.testsend.manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class Config {
    public static final ZoneId APPLICATION_TIME_ZONE_ID = ZoneId.of("JST", ZoneId.SHORT_IDS);

    @Bean
    public Clock clock() {
        return Clock.system(APPLICATION_TIME_ZONE_ID);
    }
}
