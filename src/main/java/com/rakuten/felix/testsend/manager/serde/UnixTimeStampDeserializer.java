package com.rakuten.felix.testsend.manager.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.rakuten.felix.testsend.manager.BeanConfig;
import lombok.val;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

public class UnixTimeStampDeserializer extends JsonDeserializer<ZonedDateTime> {
    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        val longValue = jsonParser.readValueAs(Long.class);
        val instant = Instant.ofEpochSecond(longValue);
        return ZonedDateTime.ofInstant(instant, BeanConfig.APPLICATION_TIME_ZONE_ID);
    }
}
