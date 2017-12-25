package com.rakuten.felix.testsend.manager.datastore.converters;

import com.rakuten.felix.testsend.manager.Config;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Optional;

@Converter
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {
    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp databaseDateTime) {
        return Optional.ofNullable(databaseDateTime)
                .map(Timestamp::toInstant)
                .map(it -> ZonedDateTime.ofInstant(it, Config.APPLICATION_TIME_ZONE_ID))
                .orElse(null);
    }

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime applicationDateTime) {
        return Optional.ofNullable(applicationDateTime)
                .map(ZonedDateTime::toInstant)
                .map(Timestamp::from)
                .orElse(null);
    }
}
