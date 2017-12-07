package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.jsonutils.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.messaging.MessageSender;
import com.rakuten.felix.testsend.manager.messaging.OutputChannels;
import com.rakuten.felix.testsend.manager.messaging.dto.KickMailTestSendMessage;
import com.rakuten.felix.testsend.manager.web.WebController;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import lombok.val;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class WebControllerTest {

    private WebController controller;

    @Mock
    private TestSendHistoryRepository repository;

    @Mock
    private MessageChannel kickTestSend;


    @BeforeEach
    void setUp() {
        initMocks(this);
        val dataStore = new DataStoreService(repository);
        val outputChannels = new OutputChannels() {
            @Override
            public MessageChannel outKickTestSend() {
                return kickTestSend;
            }

            @Override
            public MessageChannel outError() {
                return null;
            }

            @Override
            public MessageChannel outPublishNotification() {
                return null;
            }
        };
        val messageSender = new MessageSender(outputChannels,
                new ObjectMapperWrapper(new ObjectMapper()),
                "prefix.",
                "destination");
        controller = new WebController(dataStore, messageSender);
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
        // Response
        val mockedHistories = FakeData.getHistories();
        when(repository.findByBundleIdAndBundleType(bundleId, bundleType))
                .thenReturn(mockedHistories);
        // Execution
        val response = controller.getAll(bundleId, bundleType);
        // Verification
        assertNotNull(response);
        for (int i = 0; i < response.size(); i++) {
            assertHistory(mockedHistories.get(i), response.get(i));
        }
    }

    @Test
    void kickMailTestSend() throws Exception {
        // Setup
        val historyId = 12345;
        val bundleId = 1;
        val bundleType = 1;
        val mailJob = new JSONObject(Collections.singletonMap("key", "value"));
        // Response
        when(repository.saveAndFlush(any()))
                .then(it -> {
                    TestSendHistory entity = (TestSendHistory) it.getArguments()[0];
                    entity.setId(historyId);
                    return entity;
                });

        when(kickTestSend.send(any()))
                .thenReturn(true);
        // Execution
        val response = controller.kickTestSend(new KickMailTestSendRequest(bundleId, bundleType, mailJob));
        // Verification
        assertNotNull(response);

        // verify database
        val capturedEntity = ArgumentCaptor.forClass(TestSendHistory.class);
        verify(repository, times(1)).saveAndFlush(capturedEntity.capture());
        assertEquals(Integer.valueOf(bundleId), capturedEntity.getValue().getBundleId());
        assertEquals(Integer.valueOf(bundleType), capturedEntity.getValue().getBundleType());

        // verify messaging
        val capturedMessage = ArgumentCaptor.forClass(Message.class);
        verify(kickTestSend, times(1)).send(capturedMessage.capture());
        assertNotNull(capturedMessage.getValue().getHeaders().get("replyChannel"));
        val payload = capturedMessage.getValue().getPayload();
        assertTrue(payload instanceof byte[]);
        val message = new ObjectMapper().readValue((byte[]) payload, KickMailTestSendMessage.class);
        assertEquals(Integer.valueOf(historyId), message.getId());
        assertEquals(mailJob.toJSONString(), message.getMailJob());
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