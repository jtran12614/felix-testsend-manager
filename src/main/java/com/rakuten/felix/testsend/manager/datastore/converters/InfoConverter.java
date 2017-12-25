package com.rakuten.felix.testsend.manager.datastore.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Converter
public class InfoConverter implements AttributeConverter<Info, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Info data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    @Override
    public Info convertToEntityAttribute(String json) {
        return Optional.ofNullable(json)
                .map(this::mapToObject)
                .orElse(null);
    }

    private Info mapToObject(String json) {
        try {
            return MAPPER.readValue(json, Info.class);
        } catch (IOException exception) {
            log.warn("Failed to convert Info: {}", exception);
            return null;
        }
    }
}
