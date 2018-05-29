package com.rakuten.felix.testsend.manager.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ObjectMapperWrapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
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

    /**
     * Deserialize to object.
     *
     * @param json Json string.
     * @return Object.
     * @throws IllegalArgumentException When deserialization fails.
     */
    public <T> T deserializeToObject(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't deserialize to object: json=" + json, e);
        }
    }
}
