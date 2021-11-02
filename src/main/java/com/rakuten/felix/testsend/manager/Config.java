package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.common.VersionInfo;
import com.rakuten.felix.common.actuator.ActuatorConfig;
import com.rakuten.felix.common.actuator.FelixInMemoryHttpTraceRepository;

import lombok.val;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@PropertySource("classpath:/version.properties")
@Profile("!test")
public class Config {
    public static final ZoneId APPLICATION_TIME_ZONE_ID = ZoneId.of("JST", ZoneId.SHORT_IDS);

    @Bean
    public Clock clock() {
        return Clock.system(APPLICATION_TIME_ZONE_ID);
    }

    @Bean
    @ConfigurationProperties(prefix = "com.rakuten.felix.application")
    public VersionInfo versionInfo() {
        return new VersionInfo();
    }

    @Bean
    @ConfigurationProperties(prefix = "com.rakuten.felix.testsend-manager.actuator")
    public ActuatorConfig actuatorConfig() {
        return new ActuatorConfig();
    }

    @Bean
    public HttpTraceRepository httpTraceRepository(ActuatorConfig actuatorConfig) {
        val httpTraceConfig = actuatorConfig.getTrace();
        val httpTraceRepository = FelixInMemoryHttpTraceRepository.of(httpTraceConfig.getApis());
        httpTraceRepository.setCapacity(httpTraceConfig.getCapacity());
        return httpTraceRepository;
    }
}
