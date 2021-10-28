package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.common.VersionInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

    private final VersionInfo versionInfo;

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
    public void run(String... args) {
        log.info("Application started: version={}, commit={}, build start time={}", versionInfo.getVersion(), versionInfo.getCommit(), versionInfo.getBuildStartTimeAsFormattedString());
    }
}

