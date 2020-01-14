package com.rakuten.felix.testsend.manager.messaging;

import com.rakuten.felix.testsend.manager.LoggingHelper;
import com.rakuten.felix.testsend.manager.errorhandler.ErrorHandler;
import com.rakuten.felix.testsend.manager.messaging.dto.ErrorMessage;
import com.rakuten.felix.testsend.manager.messaging.dto.FinishedMessage;
import com.rakuten.felix.testsend.manager.messaging.dto.Header;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@EnableBinding(InputChannels.class)
public class MessageListener {
    private final ErrorHandler errorHandler;
    private final ObjectMapperWrapper objectMapper;
    private final Processor processor;

    /**
     * Initialize the service.
     *
     * @param objectMapper Object mapper
     * @param errorHandler Error handler.
     * @param processor    Processor.
     */
    public MessageListener(ObjectMapperWrapper objectMapper,
                           ErrorHandler errorHandler,
                           Processor processor) {
        this.objectMapper = objectMapper;
        this.errorHandler = errorHandler;
        this.processor = processor;
    }

    private void logDebug(String inputChannelName, byte[] payload) {
        log.debug("[{}]: Message received: payload={}", inputChannelName, new String(payload, StandardCharsets.UTF_8));
    }

    /**
     * Handle messages with notification about test send is finished.
     *
     * @param payload Payload.
     */
    @ServiceActivator(inputChannel = InputChannels.IN_TEST_SEND_FINISHED)
    public void testSendFinished(byte[] payload) {
        try {
            logDebug(InputChannels.IN_TEST_SEND_FINISHED, payload);
            val message = objectMapper.deserializeToObject(payload, FinishedMessage.class);
            Validator.validate(message);
            processor.processMailTestSendFinished(message.getJobId());
        } catch (Exception e) {
            errorHandler.handleExceptionWithPayload(e, payload, InputChannels.IN_TEST_SEND_FINISHED);
        }
    }

    /**
     * Handle messages with notification about test send is finished.
     *
     * @param payload Payload.
     */
    @ServiceActivator(inputChannel = InputChannels.IN_TEST_SEND_ERROR)
    public void testSendError(byte[] payload) {
        try {
            logDebug(InputChannels.IN_TEST_SEND_ERROR, payload);
            val message = objectMapper.deserializeToObject(payload, ErrorMessage.class);
            Validator.validate(message);
            processor.processTestSendError(message.getJobId(), message.getErrorMessage());
        } catch (Exception e) {
            errorHandler.handleExceptionWithPayload(e, payload, InputChannels.IN_TEST_SEND_ERROR);
        }
    }

    @ServiceActivator(inputChannel = InputChannels.JOB_MANAGER_REPLY)
    public void handleJobManagerReply(@Headers Header header, byte[] payload) throws ValidationException {
        LoggingHelper.setLogId(header.getLogId());
        log.info("Handle reply message started");
        log.debug("Input info: header=[{}], payload=[{}]", header, new String(payload, StandardCharsets.UTF_8));
        processor.handleReplyMessage(header, payload);
        log.info("Handle reply message finished");
        LoggingHelper.clear();
    }
}

