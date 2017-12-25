package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
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
import com.rakuten.felix.testsend.manager.webclients.dto.MailJob;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        initMocks(this);
        val dataStore = new DataStoreService(repository, clock);
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
        val processor = new Processor(dataStore, jobDataKeeper, new MailContentBuilder(), notificationService);
        messageListener = new MessageListener(mapperWrapper, errorHandler, processor);
    }

    @Test
    void kickTestSendFinished() throws Exception {
        // Setup
        val historyId = 12345;
        val jobId = 6789;
        val mockedHistory = FakeData.getHistory();
        val mockedMailJob = FakeData.getEmptyMailJob();
        // Response
        when(restTemplate.postForObject(GET_JOB_URL, new JobIdWrapper(jobId), MailJob.class))
                .thenReturn(mockedMailJob);
        when(repository.findById(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        // Execution
        val message = new KickedMessage(historyId, jobId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.kickTestSendFinished(payload);
        // Verification
        verify(repository, times(1)).findById(historyId);

        val capturedEntity = ArgumentCaptor.forClass(TestSendHistory.class);
        verify(repository, times(1)).saveAndFlush(capturedEntity.capture());
        val actualEntity = capturedEntity.getValue();
        assertEquals(mockedHistory.getBundleId(), actualEntity.getBundleId());
        assertEquals(mockedHistory.getBundleType(), actualEntity.getBundleType());
        assertEquals(TestSendStatus.NEW, actualEntity.getStatus());
        assertEquals(mockedHistory.getInfo().getUser(), actualEntity.getInfo().getUser());
        assertTrue(actualEntity.getInfo().getSubjects().isEmpty());
        assertTrue(actualEntity.getInfo().getHtmlContents().isEmpty());
        assertTrue(actualEntity.getInfo().getTextContents().isEmpty());

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
        val mockedHistory = FakeData.getHistory();
        val finished = ZonedDateTime.now(ZoneId.systemDefault());
        // Response
        when(repository.findById(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(clock.instant())
                .thenReturn(finished.toInstant());
        when(clock.getZone())
                .thenReturn(finished.getZone());
        // Execution
        val message = new KickedMessage(historyId, null);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.kickTestSendFinished(payload);
        // Verification
        verify(repository, times(1)).findById(historyId);

        val capturedEntity = ArgumentCaptor.forClass(TestSendHistory.class);
        verify(repository, times(1)).saveAndFlush(capturedEntity.capture());
        val actualEntity = capturedEntity.getValue();
        assertEquals(TestSendStatus.ERROR, actualEntity.getStatus());
        assertNotNull(actualEntity.getInfo());
        assertNotNull(actualEntity.getInfo().getErrorMessage());
        assertEquals("Job initialization failed", actualEntity.getInfo().getErrorMessage());

        // verify error messaging
        verify(outError, times(0)).send(any());
    }

    @Test
    void testSendFinished() throws Exception {
        // Setup
        val jobId = 11111;
        val mockedHistory = FakeData.getHistory();
        val finished = ZonedDateTime.now(ZoneId.systemDefault());
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(outPublishNotification.send(any()))
                .thenReturn(true);
        when(clock.instant())
                .thenReturn(finished.toInstant());
        when(clock.getZone())
                .thenReturn(finished.getZone());
        // Execution
        val message = new FinishedMessage(jobId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendFinished(payload);
        // Verification
        verify(repository, times(2)).findByJobId(jobId);

        val capturedEntity = ArgumentCaptor.forClass(TestSendHistory.class);
        verify(repository, times(1)).saveAndFlush(capturedEntity.capture());
        val actualEntity = capturedEntity.getValue();
        assertEquals(TestSendStatus.FINISHED, actualEntity.getStatus());
        assertEquals(finished, actualEntity.getFinished());

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
        assertEquals(Long.valueOf(mockedHistory.getInfo().getUser().getUserId()), notificationMessage.getUserId());

        // verify error messaging
        verify(outError, times(0)).send(any());
    }

    @Test
    void testSendError() throws Exception {
        // Setup
        val jobId = 123456789;
        val errorMessage = "error message";
        val mockedHistory = FakeData.getHistory();
        val finished = ZonedDateTime.now(ZoneId.systemDefault());
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(outPublishNotification.send(any()))
                .thenReturn(true);
        when(clock.instant())
                .thenReturn(finished.toInstant());
        when(clock.getZone())
                .thenReturn(finished.getZone());

        // Execution
        val message = new ErrorMessage(jobId, errorMessage);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendError(payload);
        // Verification
        verify(repository, times(2)).findByJobId(jobId);

        val capturedEntity = ArgumentCaptor.forClass(TestSendHistory.class);
        verify(repository, times(1)).saveAndFlush(capturedEntity.capture());
        val actualEntity = capturedEntity.getValue();
        assertEquals(TestSendStatus.ERROR, actualEntity.getStatus());
        assertNotNull(actualEntity.getInfo());
        assertNotNull(actualEntity.getInfo().getErrorMessage());
        assertEquals(errorMessage, actualEntity.getInfo().getErrorMessage());

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
        assertEquals(Long.valueOf(mockedHistory.getInfo().getUser().getUserId()), notificationMessage.getUserId());

        // verify error messaging
        verify(outError, times(0)).send(any());
    }
}