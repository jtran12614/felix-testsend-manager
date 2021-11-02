package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.common.VersionInfo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

import static com.rakuten.felix.testsend.manager.Config.APPLICATION_TIME_ZONE_ID;

@TestConfiguration
public class TestConf {
    @MockBean
    VersionInfo versionInfo;

    @Bean
    public Clock clock() {
        return Clock.system(APPLICATION_TIME_ZONE_ID);
    }
}
