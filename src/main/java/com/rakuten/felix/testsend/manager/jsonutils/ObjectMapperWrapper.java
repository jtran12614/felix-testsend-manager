package com.rakuten.felix.testsend.manager.jsonutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ObjectMapperWrapper {
    private final ObjectMapper objectMapper;

    public ObjectMapperWrapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serialize object to string.
     *
     * @param object Mail job object.
     * @return String.
     * @throws IllegalArgumentException When serialization fails.
     */
    public String serializeToString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Can't serialize to string: object=" + object, e);
        }
    }

    /**
     * Serialize object to bytes.
     *
     * @param object Object.
     * @return Bytes.
     * @throws IllegalArgumentException When serialization fails.
     */
    public byte[] serializeToBytes(Object object) throws IllegalArgumentException {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Can't serialize to bytes: object=" + object, e);
        }
    }

    /**
     * Deserialize to object.
     *
     * @param bytes Byte array.
     * @return Object.
     * @throws IllegalArgumentException When deserialization fails.
     */
    public <T> T deserializeToObject(byte[] bytes, Class<T> valueType) {
        try {
            return objectMapper.readValue(bytes, valueType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't deserialize to object: json=" + new String(bytes, StandardCharsets.UTF_8), e);
        }
    }
}
