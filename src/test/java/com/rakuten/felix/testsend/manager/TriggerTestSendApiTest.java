package com.rakuten.felix.testsend.manager;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.config.StorageTestConfig;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.utils.TimeUtils;
import com.rakuten.felix.testsend.manager.web.dto.RecipientData;
import com.rakuten.felix.testsend.manager.webclients.testrecipient.TestRecipientClient.TestRecipientProperties;
import com.rakuten.felix.testsend.manager.webclients.workflow.WorkFlowClient.WorkflowProperties;
import io.findify.s3mock.S3Mock;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "/application.yml", properties = "spring.hadoop.fsshell.enabled=false")
@ActiveProfiles("test")
@StorageTestConfig
public class TriggerTestSendApiTest extends AbstractIntegrationTest {

    private static final S3Mock S3_MOCK = initS3Mock();
    private static final String BUCKET_NAME = "test-storage";

    @Inject
    private MockMvc mvc;

    @MockBean
    private TimeUtils timeUtils;

    @Inject
    private JdbcTemplate jdbcTemplate;

    private MockWebServer testRecipientServer;

    private MockWebServer workflowJobStartServer;

    @Inject
    private TestRecipientProperties testRecipientProperties;

    @Inject
    private WorkflowProperties workflowProperties;

    @Autowired
    private TestSendHistoryRepository testSendHistoryRepository;

    @BeforeEach
    public void init() throws IOException {
        super.init();
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();

        testRecipientServer = new MockWebServer();
        testRecipientServer.start();
        testRecipientProperties.url = testRecipientServer.url("/test-recipient-groups/recipients").uri().toString();

        workflowJobStartServer = new MockWebServer();
        workflowJobStartServer.start();
        workflowProperties.endpoints.base = workflowJobStartServer.url("/api/v1/jobs").uri().toString();
    }

    @AfterEach
    public void stop() throws IOException {
        super.stop();
        testRecipientServer.shutdown();
        workflowJobStartServer.shutdown();
    }

    @AfterAll
    public static void finish() {
        S3_MOCK.shutdown();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideTestData")
    @SqlGroup({
            @Sql(scripts = "classpath:/sql/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
            @Sql(scripts = "classpath:/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    })
    public void testSendApiV2Test(String name, TestData testData) throws Exception {
        if (CollectionUtils.isNotEmpty(testData.dataPaths)) {
            testData.dataPaths.stream()
                    .map(resourceLoader::getResource)
                    .map(this::readResource)
                    .peek(sql -> log.info("Execute SQL: {}", sql))
                    .forEach(jdbcTemplate::execute);
        }
        val timeStamp = "2022-08-22T11:53:51.000+09:00[Asia/Tokyo]";
        when(timeUtils.getCurrentTime(any(Clock.class))).thenReturn(ZonedDateTime.parse(timeStamp));
        val testRecipientApiData = registerServerResponses(testData.testRecipientApiPath, testRecipientServer);
        val workFlowApiData = registerServerResponses(testData.workFlowApiPath, workflowJobStartServer);
        val apiData = loadData(testData.apiDataPath, ApiData.class);
        val headers = new HttpHeaders();
        val requestBody = apiData.request.body;
        apiData.request.headers.forEach((key, value) -> headers.add(key, String.valueOf(value)));
        val expected = mvc.perform(MockMvcRequestBuilders.post("/api/v1/test-send/trigger")
                        .headers(headers)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(apiData.response.status));
        if (apiData.response.body instanceof String) {
            expected.andExpect(content().string((String) apiData.response.body));
        } else {
            expected.andExpect(content().json(new ObjectMapper().writeValueAsString(apiData.response.body)));
        }
        assertEquals(testRecipientApiData.size(), testRecipientServer.getRequestCount(), "Mismatch Get Test Recipient API executions");
        validateServerExecution(testRecipientApiData, testRecipientServer, "Test Recipient Server", (data, path) -> assertEquals("GET /test-recipient-groups/recipients?groupId=114 HTTP/1.1", path));
        assertEquals(workFlowApiData.size(), workflowJobStartServer.getRequestCount(), "Mismatch Post Creat workflow API executions");
        validateServerExecution(workFlowApiData, workflowJobStartServer, "Create Workflow Server", (data, path) -> assertEquals("POST /api/v1/jobs?async=true HTTP/1.1", path));
        if (apiData.response.status == 200) {
            val map = (Map) requestBody;
            assertEquals(1, testSendHistoryRepository.count());
            val savedHistory = testSendHistoryRepository.findById(1).get();
            val expectedHistory = loadData(testData.testSendHistoryPath,TestSendHistory.class);
            assertEquals(expectedHistory.toString(), savedHistory.toString());
            val filePath = mapper.convertValue(map.get("recipientData"), RecipientData.class).getRecipientFilePath().substring(1);
            val object = s3client.getObject(new GetObjectRequest(BUCKET_NAME, filePath));
            val actualData = new StringBuilder();
            val reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                actualData.append(line);
            }
            val expectedData = readResource(testData.recipientFilePath);
            assertEquals(expectedData, actualData.toString());
        }
    }

    public static Stream<Arguments> provideTestData() throws IOException {
        val mapper = new ObjectMapper();
        val testCaseFile = TriggerTestSendApiTest.class.getClassLoader().getResource("test-cases/trigger-test-send.json");
        val testCases = mapper.readValue(testCaseFile, new TypeReference<List<TriggerTestSendApiTest.TestData>>() {
        });
        return testCases.stream().map(testData -> Arguments.of(testData.name, testData));
    }

    @Override
    public S3Mock getS3Mock() {
        return S3_MOCK;
    }

    @Builder(toBuilder = true)
    public static class TestData {
        String name;
        String apiDataPath;
        List<String> workFlowApiPath;
        List<String> testRecipientApiPath;
        List<String> dataPaths;
        String recipientFilePath;
        String testSendHistoryPath;
    }
}
