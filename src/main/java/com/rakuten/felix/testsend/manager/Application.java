package com.rakuten.felix.testsend.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@Slf4j
public class Application implements ApplicationListener<ContextRefreshedEvent> {
    private final VersionInfo versionInfo;

    public Application(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    /**
     * Application entry.
     *
     * @param args Arguments.
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .properties("spring.config.name=application,version")
                .listeners(new ApplicationPidFileWriter())
                .run(args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Application started: version={}, commit={}, build start time={}", versionInfo.getVersion(), versionInfo.getCommit(), versionInfo.getBuildStartTimeAsFormattedString());
    }
}

