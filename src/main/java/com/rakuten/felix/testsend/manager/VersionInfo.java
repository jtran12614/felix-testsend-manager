package com.rakuten.felix.testsend.manager;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

@Data
@Component
@ConfigurationProperties(prefix = "com.rakuten.felix.application")
public class VersionInfo {
    private String version;
    private String commit;
    private Long buildStartTime;

    private final Function<Long, String> convertToString = it -> LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    /**
     * Return human readable date of build.
     *
     * @return Date of build as human readable string.
     */
    public String getBuildStartTimeAsFormattedString() {
        return Optional.ofNullable(buildStartTime)
                       .map(convertToString)
                       .orElse("n/a");
    }
}