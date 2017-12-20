package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.errorhandler.ErrorHandler;
import com.rakuten.felix.testsend.manager.jsonutils.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.messaging.MessageListener;
import com.rakuten.felix.testsend.manager.messaging.MessageSender;
import com.rakuten.felix.testsend.manager.messaging.NotificationService;
import com.rakuten.felix.testsend.manager.messaging.OutputChannels;
import com.rakuten.felix.testsend.manager.messaging.dto.ErrorMessage;
import com.rakuten.felix.testsend.manager.messaging.dto.FinishedMessage;
import com.rakuten.felix.testsend.manager.messaging.dto.KickedMessage;
import com.rakuten.felix.testsend.manager.messaging.dto.Notification;
import com.rakuten.felix.testsend.manager.processor.MailContentBuilder;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.webclients.JobDataKeeperService;
import com.rakuten.felix.testsend.manager.webclients.dto.JobIdWrapper;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJobWithContents;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MessageListenerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GET_JOB_URL = "http://job-data-keeper/get-job-url";
    private static final String NOTIFICATION_URL = "http://notification-url/{bundle_id}";
    private static final String NOTIFICATION_SUCCESS_TITLE = "success title: {bundle_id}";
    private static final String NOTIFICATION_SUCCESS_MESSAGE = "success message: {bundle_id}";
    private static final String NOTIFICATION_ERROR_TITLE = "error title: {bundle_id}";
    private static final String NOTIFICATION_ERROR_MESSAGE = "error message: {bundle_id}";

    private MessageListener messageListener;

    @Mock
    private TestSendHistoryRepository repository;

    @Mock
    private MessageChannel kickTestSend;

    @Mock
    private MessageChannel outError;

    @Mock
    private MessageChannel outPublishNotification;

    @Mock
    private RestTemplate restTemplate;

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
                return outError;
            }

            @Override
            public MessageChannel outPublishNotification() {
                return outPublishNotification;
            }
        };
        val mapperWrapper = new ObjectMapperWrapper(new ObjectMapper());
        val messageSender = new MessageSender(outputChannels, mapperWrapper, null, null);
        val errorHandler = new ErrorHandler(messageSender);
        val jobDataKeeper = new JobDataKeeperService(GET_JOB_URL, restTemplate);
        val notificationService = new NotificationService(NOTIFICATION_URL,
                NOTIFICATION_SUCCESS_TITLE,
                NOTIFICATION_SUCCESS_MESSAGE,
                NOTIFICATION_ERROR_TITLE,
                NOTIFICATION_ERROR_MESSAGE,
                messageSender);
        val processor = new Processor(dataStore, mapperWrapper, jobDataKeeper, new MailContentBuilder(), notificationService);
        messageListener = new MessageListener(mapperWrapper, errorHandler, processor);
    }

    @Test
    void kickTestSendFinished() throws Exception {
        // Setup
        val historyId = 12345;
        val jobId = 6789;
        // Response
        when(repository.updateJobId(anyInt(), anyInt()))
                .thenReturn(1);
        // Execution
        val message = new KickedMessage(historyId, jobId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.kickTestSendFinished(payload);
        // Verification
        verify(repository, times(1)).updateJobId(historyId, jobId);

        // verify error messaging
        verify(outError, times(0)).send(any());
    }

    @Test
    void kickTestSendFinished_dataNotFound() throws Exception {
        // Setup
        val historyId = 12345;
        val jobId = 6789;
        // Response
        when(repository.updateJobId(anyInt(), anyInt()))
                .thenReturn(0);
        // Execution
        val message = new KickedMessage(historyId, jobId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.kickTestSendFinished(payload);
        // Verification
        val capturedMessage = ArgumentCaptor.forClass(Message.class);
        verify(outError, times(1)).send(capturedMessage.capture());
        assertNotNull(capturedMessage.getValue().getPayload());
    }

    @Test
    void kickTestSendFinished_jobIdNull() throws Exception {
        // Setup
        val historyId = 12345;
        // Response
        when(repository.updateInfoAndStatusErrorById(anyInt(), anyString()))
                .thenReturn(1);
        // Execution
        val message = new KickedMessage(historyId, null);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.kickTestSendFinished(payload);
        // Verification
        verify(repository, times(1)).updateInfoAndStatusErrorById(historyId, "{\"errorMessage\":\"Job initialization failed\"}");

        // verify error messaging
        verify(outError, times(0)).send(any());
    }

    @Test
    void testSendFinished() throws Exception {
        // Setup
        val jobId = 11111;
        val scheduleId = 0;
        val mockedHistory = FakeData.getHistory();
        // Response
        when(restTemplate.postForObject(GET_JOB_URL, new JobIdWrapper(jobId), MailJobWithContents.class))
                .thenReturn(FakeData.getEmptyMailJob());
        when(repository.updateInfoAndStatusFinished(anyInt(), anyString()))
                .thenReturn(1);
        when(outPublishNotification.send(any()))
                .thenReturn(true);
        when(repository.findByJobId(jobId))
                .thenReturn(Optional.of(mockedHistory));
        // Execution
        val message = new FinishedMessage(jobId, scheduleId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendFinished(payload);
        // Verification
        val expectedInfoJson = "{\"subjects\":[],\"htmlContents\":[],\"textContents\":[],\"user\":{\"userId\":0,\"mailAddress\":\"\"}}";
        verify(repository, times(1)).updateInfoAndStatusFinished(jobId, expectedInfoJson);

        // verify notification messaging
        val capturedMessage = ArgumentCaptor.forClass(Message.class);
        verify(outPublishNotification, times(1)).send(capturedMessage.capture());
        val notificationPayload = capturedMessage.getValue().getPayload();
        assertTrue(notificationPayload instanceof byte[]);
        val notificationMessage = new ObjectMapper().readValue((byte[]) notificationPayload, Notification.class);
        val bundleId = mockedHistory.getBundleId().toString();
        assertEquals(NOTIFICATION_URL.replaceAll("\\{bundle_id}", bundleId), notificationMessage.getUrl());
        assertEquals(NOTIFICATION_SUCCESS_TITLE.replaceAll("\\{bundle_id}", bundleId), notificationMessage.getTitle());
        assertEquals(NOTIFICATION_SUCCESS_MESSAGE.replaceAll("\\{bundle_id}", bundleId), notificationMessage.getMessage());
        assertEquals(Boolean.TRUE, notificationMessage.getNoticeFlag());
        assertEquals(Long.valueOf(0), notificationMessage.getUserId());

        // verify error messaging
        verify(outError, times(0)).send(any());
    }

    @Test
    void testSendFinished_validationFailed() throws Exception {
        // Setup
        val jobId = 11111;
        val scheduleId = 0;
        // Response
        when(restTemplate.postForObject(GET_JOB_URL, new JobIdWrapper(jobId), MailJobWithContents.class))
                .thenReturn(new MailJobWithContents(null, null, null));
        when(repository.updateStatusErrorByJobId(anyInt()))
                .thenReturn(1);
        // Execution
        val message = new FinishedMessage(jobId, scheduleId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendFinished(payload);
        // Verification
        val capturedMessage = ArgumentCaptor.forClass(Message.class);
        verify(outError, times(1)).send(capturedMessage.capture());
        assertNotNull(capturedMessage.getValue().getPayload());
    }

    @Test
    void testSendError() throws Exception {
        // Setup
        val jobId = 123456789;
        val errorMessage = "error message";
        val mockedHistory = FakeData.getHistory();
        // Response
        when(restTemplate.postForObject(GET_JOB_URL, new JobIdWrapper(jobId), MailJobWithContents.class))
                .thenReturn(FakeData.getEmptyMailJob());
        when(repository.updateInfoAndStatusError(anyInt(), anyString()))
                .thenReturn(1);
        when(outPublishNotification.send(any()))
                .thenReturn(true);
        when(repository.findByJobId(jobId))
                .thenReturn(Optional.of(mockedHistory));
        // Execution
        val message = new ErrorMessage(jobId, errorMessage);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendError(payload);
        // Verification
        val expectedInfoJson = "{\"errorMessage\":\"error message\",\"user\":{\"userId\":0,\"mailAddress\":\"\"}}";
        verify(repository, times(1)).updateInfoAndStatusError(jobId, expectedInfoJson);

        // verify notification messaging
        val capturedMessage = ArgumentCaptor.forClass(Message.class);
        verify(outPublishNotification, times(1)).send(capturedMessage.capture());
        val notificationPayload = capturedMessage.getValue().getPayload();
        assertTrue(notificationPayload instanceof byte[]);
        val notificationMessage = new ObjectMapper().readValue((byte[]) notificationPayload, Notification.class);
        val bundleId = mockedHistory.getBundleId().toString();
        assertEquals(NOTIFICATION_URL.replaceAll("\\{bundle_id}", bundleId), notificationMessage.getUrl());
        assertEquals(NOTIFICATION_ERROR_TITLE.replaceAll("\\{bundle_id}", bundleId), notificationMessage.getTitle());
        assertEquals(NOTIFICATION_ERROR_MESSAGE.replaceAll("\\{bundle_id}", bundleId), notificationMessage.getMessage());
        assertEquals(Boolean.TRUE, notificationMessage.getNoticeFlag());
        assertEquals(Long.valueOf(0), notificationMessage.getUserId());

        // verify error messaging
        verify(outError, times(0)).send(any());
    }

    @Test
    void testSendError_validationFailed() throws Exception {
        // Setup
        val jobId = 123456789;
        val errorMessage = "error message";
        // Response
        when(restTemplate.postForObject(GET_JOB_URL, new JobIdWrapper(jobId), MailJobWithContents.class))
                .thenReturn(new MailJobWithContents(null, null, null));
        // Execution
        val message = new ErrorMessage(jobId, errorMessage);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendError(payload);
        // Verification
        // verify error messaging
        val capturedMessage = ArgumentCaptor.forClass(Message.class);
        verify(outError, times(1)).send(capturedMessage.capture());
        assertNotNull(capturedMessage.getValue().getPayload());
    }
}