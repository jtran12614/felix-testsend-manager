package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.processor.MailContentBuilder;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.web.WebController;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import com.rakuten.felix.testsend.manager.webclients.CampaignSchedulerService;
import com.rakuten.felix.testsend.manager.webclients.dto.RegisterCampaignResponse;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.val;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class WebControllerTest {
    private static final String SCHEDULER_REGISTER_AND_GET_URL = "http://campaign-scheduler/register-and-get/url";
    private final ObjectMapper mapper = new ObjectMapper();

    private WebController controller;
    @Mock
    private TestSendHistoryRepository repository;
    @Mock
    private Clock clock;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        initMocks(this);
        val dataStore = new DataStoreService(repository, clock);
        val schedulerService = new CampaignSchedulerService(SCHEDULER_REGISTER_AND_GET_URL, restTemplate);
        val processor = new Processor(dataStore, new MailContentBuilder(), null, schedulerService, new ObjectMapperWrapper());

        controller = new WebController(dataStore, processor);
    }

    @Test
    void getHistoryById() {
        // Setup
        val historyId = 1;
        // Response
        val mockedHistory = FakeData.getHistory();
        when(repository.findById(historyId))
                .thenReturn(Optional.of(mockedHistory));
        // Execution
        val response = controller.get(historyId);
        // Verification
        assertNotNull(response);
        assertHistory(mockedHistory, response);
    }

    @Test
    void getHistoryByJobId() {
        // Setup
        val jobId = 1;
        // Response
        val mockedHistory = FakeData.getHistory();
        when(repository.findByJobId(jobId))
                .thenReturn(Optional.of(mockedHistory));
        // Execution
        val response = controller.getBydJobId(jobId);
        // Verification
        assertNotNull(response);
        assertHistory(mockedHistory, response);
    }

    @Test
    void getHistoriesByBundleIdAndBundleType() {
        // Setup
        val bundleId = 1;
        val bundleType = 1;
        val page = PageRequest.of(1, 2);
        // Response
        val mockedHistories = FakeData.getHistories();
        when(repository.findByBundleIdAndBundleType(bundleId, bundleType, page))
                .thenReturn(new PageImpl<>(mockedHistories));
        // Execution
        val response = controller.getAll(bundleId, bundleType, page);
        // Verification
        assertNotNull(response);
        for (int i = 0; i < response.getContent().size(); i++) {
            assertHistory(mockedHistories.get(i), response.getContent().get(i));
        }
    }

    @Test
    void kickMailTestSend() throws Exception {
        // Setup
        val historyId = 12345;
        val bundleId = 1;
        val bundleType = 1;
        val jdkId = 123;
        val mailJob = FakeData.getValidMailJob();
        val mailJobJsonObj = mapper.readValue(mapper.writeValueAsString(mailJob), JSONObject.class);
        val started = ZonedDateTime.now(ZoneId.systemDefault());
        val user = new User(1, "name", "mail-address");
        // Response
        when(repository.saveAndFlush(any()))
                .then(it -> {
                    TestSendHistory entity = (TestSendHistory) it.getArguments()[0];
                    entity.setId(historyId);
                    return entity;
                });

        when(clock.instant())
                .thenReturn(started.toInstant());
        when(clock.getZone())
                .thenReturn(started.getZone());

        when(restTemplate.postForObject(eq(SCHEDULER_REGISTER_AND_GET_URL), any(), eq(RegisterCampaignResponse.class)))
                .thenReturn(new RegisterCampaignResponse(jdkId));
        // Execution
        val response = controller.kickTestSend(new KickMailTestSendRequest(bundleId, bundleType, mailJobJsonObj, user));
        // Verification
        assertNotNull(response);

        // verify database
        val capturedEntity = ArgumentCaptor.forClass(TestSendHistory.class);
        verify(repository, times(1)).saveAndFlush(capturedEntity.capture());
        val actualEntity = capturedEntity.getValue();
        assertEquals(TestSendStatus.NEW, actualEntity.getStatus());
        assertEquals(TestSendStatus.NEW, TestSendStatus.fromNumber(actualEntity.getStatus().toNumber()));
        assertEquals(user, actualEntity.getInfo().getUser());
        assertEquals(Integer.valueOf(bundleId), actualEntity.getBundleId());
        assertEquals(Integer.valueOf(bundleType), actualEntity.getBundleType());
        assertEquals(Integer.valueOf(jdkId), actualEntity.getJobId());
        assertEquals(started, actualEntity.getStarted());
        assertEquals(user, actualEntity.getInfo().getUser());
        assertEquals(actualEntity.getInfo().getSubjects(), Collections.singletonList("Part0Part1Part2"));
        assertEquals(actualEntity.getInfo().getHtmlContents(), Collections.singletonList("Part0Part1Part2"));
        assertTrue(actualEntity.getInfo().getTextContents().isEmpty());
        assertEquals(actualEntity.getInfo().getRecipients(), mailJob.getPrependAddresses());
    }

    private void assertHistory(TestSendHistory mockedHistory, TestSendHistory result) {
        assertEquals(mockedHistory.getId(), result.getId());
        assertEquals(mockedHistory.getBundleId(), result.getBundleId());
        assertEquals(mockedHistory.getBundleType(), result.getBundleType());
        assertEquals(mockedHistory.getJobId(), result.getJobId());
        assertEquals(mockedHistory.getStatus(), result.getStatus());
        assertEquals(mockedHistory.getInfo(), result.getInfo());
    }
}