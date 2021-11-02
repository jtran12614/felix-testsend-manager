package com.rakuten.felix.testsend.manager.web;

import com.rakuten.felix.common.web.dto.AuthHeader;
import com.rakuten.felix.testsend.manager.TestConf;
import com.rakuten.felix.testsend.manager.TestUtils;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static com.rakuten.felix.testsend.manager.Config.APPLICATION_TIME_ZONE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConf.class)
public class WebControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    TestSendHistoryRepository testSendHistoryRepository;

    @Autowired
    ObjectMapper mapper;

    static String testSendHistoryInitReq;

    @BeforeAll
    static void beforeAll() {
        testSendHistoryInitReq = TestUtils.readFileToString("dto/TestSendHistoryInitializeRequest.json");
    }

    @Test
    @Sql(scripts = "/schema.sql")
    void testHistoryGetWithInvalidAuth() {
        val url = URI.create("/testsend-manager/histories/1");
        val request = RequestEntity
                .get(url)
                .accept(MediaType.APPLICATION_JSON)
                .build();
        val response = restTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(scripts = "/schema.sql")
    void testHistoryGetWithValidRequest() {
        assertEquals(0, testSendHistoryRepository.count());
        val started = Instant.ofEpochSecond(1633860000).atZone(APPLICATION_TIME_ZONE_ID);
        val finished = Instant.ofEpochSecond(1633860060).atZone(APPLICATION_TIME_ZONE_ID);
        val testSendHistoryId = 1;
        val savedTestSendHistory = testSendHistoryRepository.save(
                TestSendHistory
                        .builder()
                        .jobId(1)
                        .bundleId(2)
                        .bundleType(3)
                        .status(TestSendStatus.NEW)
                        .info(Info.builder().build())
                        .started(started)
                        .finished(finished)
                        .build());
        assertEquals(testSendHistoryId, savedTestSendHistory.getId());

        val url = URI.create("/testsend-manager/histories/" + testSendHistoryId);
        val request = RequestEntity
                .get(url)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.set(AuthHeader.HEADER_AUTH, "example-auth-key"))
                .build();
        val response = restTemplate.exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList(MediaType.APPLICATION_JSON_VALUE), response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        val responseBody = new String(response.getBody(), StandardCharsets.UTF_8);
        val expectedRawJson = "{\"id\":1,\"jobId\":1,\"bundleType\":3,\"bundleId\":2,\"status\":0,\"info\":{},\"started\":1633860000,\"finished\":1633860060}";
        assertEquals(expectedRawJson, responseBody);
    }

    @Test
    @Sql(scripts = "/schema.sql")
    void testHistoryPost() {
        assertEquals(0, testSendHistoryRepository.count());
        val startTime = ZonedDateTime.now();

        // send request
        val url = URI.create("/testsend-manager/api/v1/histories");
        val request = RequestEntity
                .post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.set(AuthHeader.HEADER_AUTH, "example-auth-key"))
                .body(testSendHistoryInitReq);
        val response = restTemplate.exchange(request, byte[].class);

        // verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Arrays.asList(MediaType.APPLICATION_JSON_VALUE), response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        val responseBody = new String(response.getBody(), StandardCharsets.UTF_8);
        assertEquals("{\"testSendHistoryId\":1}", responseBody);

        // verify stored data
        assertEquals(1, testSendHistoryRepository.count());
        val savedHistory = testSendHistoryRepository.findByJobId(1).get();
        assertEquals(1, savedHistory.getId());
        assertEquals(1, savedHistory.getJobId());
        assertEquals(2, savedHistory.getBundleId());
        assertEquals(3, savedHistory.getBundleType());
        assertEquals(TestSendStatus.NEW, savedHistory.getStatus());
        assertNotNull(savedHistory.getInfo()); // NOTE: info contains only metadata so no need to verify actual content

        val now = ZonedDateTime.now();
        assertTrue(savedHistory.getStarted().isAfter(startTime));
        assertTrue(savedHistory.getStarted().isBefore(now));
        assertNull(savedHistory.getFinished());
    }
}
