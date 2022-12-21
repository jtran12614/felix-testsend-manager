package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.web.WebController;
import io.findify.s3mock.S3Mock;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
@ContextConfiguration(classes = { WebController.class, ValidationAutoConfiguration.class})
public class InitializeTestSendHistoryApiTest extends AbstractIntegrationTest {
    @Inject
    private MockMvc mvc;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestSendHistoryRepository testSendHistoryRepository;

    @Override
    protected S3Mock getS3Mock() {
        return null;
    }

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();
    }


    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideTestData")
    @SqlGroup({
            @Sql(scripts = "classpath:/sql/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
            @Sql(scripts = "classpath:/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    })
    public void initializeTestSendHistoryTest(String name, TestData testData) throws Exception {
        if (CollectionUtils.isNotEmpty(testData.dataPaths)) {
            testData.dataPaths.stream()
                    .map(resourceLoader::getResource)
                    .map(this::readResource)
                    .peek(sql -> log.info("Execute SQL: {}", sql))
                    .forEach(jdbcTemplate::execute);
        }

        val apiData = loadData(testData.apiDataPath, ApiData.class);
        val headers = new HttpHeaders();
        val requestBody = apiData.request.body;
        apiData.request.headers.forEach((key, value) -> headers.add(key, String.valueOf(value)));
        val expected = mvc.perform(MockMvcRequestBuilders.post("/testsend-manager/api/v1/histories")
                        .headers(headers)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(apiData.response.status));
        if (apiData.response.body instanceof String) {
            expected.andExpect(content().string((String) apiData.response.body));
        } else {
            expected.andExpect(content().json(new ObjectMapper().writeValueAsString(apiData.response.body)));
        }

        if (apiData.response.status == 200) {
            assertEquals(1, testSendHistoryRepository.count());
        }
    }

    public static Stream<Arguments> provideTestData() throws IOException {
        val mapper = new ObjectMapper();
        val testCaseFile = InitializeTestSendHistoryApiTest.class.getClassLoader().getResource("test-cases/initialize-test-send-history.json");
        val testCases = mapper.readValue(testCaseFile, new TypeReference<List<InitializeTestSendHistoryApiTest.TestData>>() {
        });
        return testCases.stream().map(testData -> Arguments.of(testData.name, testData));
    }

    @Builder(toBuilder = true)
    public static class TestData {
        String name;
        String apiDataPath;
        List<String> dataPaths;
    }
}
