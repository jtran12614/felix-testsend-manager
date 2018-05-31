package com.rakuten.felix.testsend.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.errorhandler.ErrorHandler;
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
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class MessageListenerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String NOTIFICATION_URL = "http://notification-url/{bundle_id}";
    private static final String NOTIFICATION_SUCCESS_TITLE = "success title: {bundle_id}";
    private static final String NOTIFICATION_SUCCESS_MESSAGE = "success message: {bundle_id}";
    private static final String NOTIFICATION_ERROR_TITLE = "error title: {bundle_id}";
    private static final String NOTIFICATION_ERROR_MESSAGE = "error message: {bundle_id}";
    private static final Clock clock = Clock.systemDefaultZone();

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
        val dataStore = new DataStoreService(repository, clock);
        val outputChannels = new OutputChannels() {
            @Override
            public MessageChannel outError() {
                return outError;
            }

            @Override
            public MessageChannel outPublishNotification() {
                return outPublishNotification;
            }
        };
        val messageSender = new MessageSender(outputChannels, new ObjectMapperWrapper(), clock);
        val errorHandler = new ErrorHandler(messageSender);
        val notificationService = new NotificationService(NOTIFICATION_URL,
                NOTIFICATION_SUCCESS_TITLE,
                NOTIFICATION_SUCCESS_MESSAGE,
                NOTIFICATION_ERROR_TITLE,
                NOTIFICATION_ERROR_MESSAGE,
                messageSender);
        val processor = new Processor(dataStore, new MailContentBuilder(), notificationService, null, new ObjectMapperWrapper());
        messageListener = new MessageListener(new ObjectMapperWrapper(), errorHandler, processor);
    }

    @Test
    void testSendFinished() throws Exception {
        // Setup
        val jobId = 11111;
        val mockedHistory = FakeData.getHistory();
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(outPublishNotification.send(any()))
                .thenReturn(true);
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
    void testSendFinished_NotHistory() throws Exception {
        // Setup
        val jobId = 11111;
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.empty());
        when(outPublishNotification.send(any()))
                .thenReturn(false);
        // Execution
        val message = new FinishedMessage(jobId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendFinished(payload);
        // Verification
        verify(repository, times(1)).findByJobId(jobId);
    }

    @Test
    void testSendFinished_Exception() throws Exception {
        // Setup
        val jobId = 11111;
        val mockedHistory = FakeData.getHistory();
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(outPublishNotification.send(any()))
                .thenReturn(false);
        // Execution
        val message = new FinishedMessage(jobId);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendFinished(payload);
        // Verification
        verify(repository, times(3)).findByJobId(jobId);
        // verify error messaging
        verify(outError, times(1)).send(any());
    }

    @Test
    void testSendError() throws Exception {
        // Setup
        val jobId = 123456789;
        val errorMessage = "error message";
        val mockedHistory = FakeData.getHistory();
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(outPublishNotification.send(any()))
                .thenReturn(true);

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

    @Test
    void testSendError_NotHistory() throws JsonProcessingException {
        // Setup
        val jobId = 123456789;
        val errorMessage = "error message";
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.empty());
        when(outPublishNotification.send(any()))
                .thenReturn(false);

        // Execution
        val message = new ErrorMessage(jobId, errorMessage);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendError(payload);
        // Verification
        verify(repository, times(1)).findByJobId(jobId);
    }

    @Test
    void testSendError_Exception() throws JsonProcessingException {
        // Setup
        val jobId = 123456789;
        val errorMessage = "error message";
        val mockedHistory = FakeData.getHistory();
        // Response
        when(repository.findByJobId(anyInt()))
                .thenReturn(Optional.of(mockedHistory));
        when(outPublishNotification.send(any()))
                .thenReturn(false);

        // Execution
        val message = new ErrorMessage(jobId, errorMessage);
        val payload = MAPPER.writeValueAsBytes(message);
        messageListener.testSendError(payload);
        // Verification
        verify(repository, times(3)).findByJobId(jobId);

        // verify error messaging
        verify(outError, times(1)).send(any());
    }

    @Test
    void deserializeToObject_Exception() {
        ObjectMapperWrapper objectMapperWrapper = new ObjectMapperWrapper();
        val mailTestSendPayload = objectMapperWrapper.serializeToBytes(new TestSendHistory().toString());
        assertThrows(IllegalArgumentException.class, () -> objectMapperWrapper.deserializeToObject(mailTestSendPayload, KickedMessage.class));
    }
}