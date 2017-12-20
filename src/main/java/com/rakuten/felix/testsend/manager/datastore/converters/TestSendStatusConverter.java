package com.rakuten.felix.testsend.manager.datastore.converters;

import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Optional;

@Converter
public class TestSendStatusConverter implements AttributeConverter<TestSendStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TestSendStatus state) {
        return Optional.ofNullable(state)
                .map(TestSendStatus::toNumber)
                .orElse(null);
    }

    @Override
    public TestSendStatus convertToEntityAttribute(Integer number) {
        return Optional.ofNullable(number)
                .map(TestSendStatus::fromNumber)
                .orElse(null);
    }
}
