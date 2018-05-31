package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.converters.InfoConverter;
import com.rakuten.felix.testsend.manager.datastore.converters.TestSendStatusConverter;
import com.rakuten.felix.testsend.manager.datastore.converters.ZonedDateTimeConverter;
import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConverterTest {

    @Test
    void sendStatusConverter() {
        val testObject = new TestSendStatusConverter();
        assertEquals((Integer) 0, testObject.convertToDatabaseColumn(TestSendStatus.NEW));
        assertEquals(TestSendStatus.FINISHED, testObject.convertToEntityAttribute(1));
    }

    @Test
    void infoConverter_convertToDatabaseColumn() {
        val testObject = new InfoConverter();
        val info = Info.builder()
                       .subjects(Collections.singletonList("Subject"))
                       .htmlContents(Collections.singletonList("Html content"))
                       .textContents(Collections.singletonList("Text content"))
                       .user(new User(1, "user-name", "user-address@rakuten.com"))
                       .recipients(Arrays.asList("recipient1", "recipient2"))
                       .build();
        val actual = testObject.convertToDatabaseColumn(info);
        assertNotNull(actual);
    }

    @Test
    void infoConverter_convertToEntityAttribute() throws JsonProcessingException {
        val testObject = new InfoConverter();
        val info = Info.builder()
                       .subjects(Collections.singletonList("Subject"))
                       .htmlContents(Collections.singletonList("Html content"))
                       .textContents(Collections.singletonList("Text content"))
                       .user(new User(1, "user-name", "user-address@rakuten.com"))
                       .recipients(Arrays.asList("recipient1", "recipient2"))
                       .build();
        val payload = new ObjectMapper().writeValueAsString(info);
        val actual = testObject.convertToEntityAttribute(payload);
        assertNotNull(actual);
        assertEquals(info.getSubjects(), actual.getSubjects());
    }

    @Test
    void infoConverter_convertToEntityAttribute_ParseFail() throws JsonProcessingException {
        val testObject = new InfoConverter();
        val history = FakeData.getHistory();
        val payload = new ObjectMapper().writeValueAsString(history);
        val actual = testObject.convertToEntityAttribute(payload);
        assertNull(actual);
    }

    @Test
    void zonedDateTimeConverter_convertToEntityAttribute() {
        val testObject = new ZonedDateTimeConverter();
        val now = ZonedDateTime.now();
        val timeStamp = new Timestamp(Date.from(Instant.from(now)).getTime());
        val actual = testObject.convertToEntityAttribute(timeStamp);
        assertNotNull(actual);
        assertEquals(now.getDayOfYear(), actual.getDayOfYear());
        assertEquals(now.getMonthValue(), actual.getMonthValue());
        assertEquals(now.getDayOfMonth(), actual.getDayOfMonth());
    }

    @Test
    void zonedDateTimeConverter_convertToDatabaseColumn() {
        val testObject = new ZonedDateTimeConverter();
        val now = ZonedDateTime.now();
        val timeStamp = new Timestamp(Date.from(Instant.from(now)).getTime());
        val actual = testObject.convertToDatabaseColumn(now);
        assertNotNull(actual);
        assertEquals(timeStamp, actual);
    }
}
