package com.rakuten.felix.testsend.manager.messaging;

import com.rakuten.felix.testsend.manager.messaging.dto.Notification;
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.webclients.dto.LineJob;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@EnableBinding(OutputChannels.class)
public class MessageSender {
    private final OutputChannels outputChannels;
    private final ObjectMapperWrapper objectMapperWrapper;
    private final Clock clock;

    /**
     * Initialize service.
     *
     * @param outputChannels      Output channels.
     * @param objectMapperWrapper JSON object mapper wrapper.
     * @param clock               Clock.
     */
    @Autowired
    public MessageSender(OutputChannels outputChannels,
                         ObjectMapperWrapper objectMapperWrapper,
                         Clock clock) {

        this.outputChannels = outputChannels;
        this.objectMapperWrapper = objectMapperWrapper;
        this.clock = clock;
    }

    /**
     * Send a message to publish notification channel.
     *
     * @param notification Notification
     */
    public void publishNotification(Notification notification) {
        val payload = objectMapperWrapper.serializeToBytes(notification);
        sendMessage(outputChannels.outPublishNotification(), MessageBuilder.withPayload(payload).build(), "Could not send publish notification");
    }

    /**
     * Send message to a channel. If message can't be send, exception will be thrown with error messages
     * join together without any delimiter.
     *
     * @param channel       Channel.
     * @param message       Message.
     * @param errorMessages Error message parts.  @throws MessageSendException When send failed.
     */
    private void sendMessage(MessageChannel channel, Message<?> message, CharSequence... errorMessages) {
        val sendResult = channel.send(message);
        if (!sendResult) {
            final String errorMessage = String.join("", errorMessages);
            throw new MessageSendException(errorMessage);
        }
    }

    /**
     * Send a message to job init error channel.
     *
     * @param payload          Payload.
     * @param message          Error message.
     * @param inputChannelName Channel name.
     */
    public void sendErrorMessage(byte[] payload, String message, String inputChannelName) {
        sendErrorMessage(payload, message, inputChannelName, outputChannels.outError());
    }

    /**
     * Send a message. If can'r be send, error is logged to a file.
     *
     * @param payload          Payload.
     * @param message          Error message.
     * @param inputChannelName Channel name.
     */
    private void sendErrorMessage(byte[] payload, String message, String inputChannelName, MessageChannel errorChannel) {
        val now = LocalDateTime.now(clock);
        final byte[] payloadNormalized = Optional.ofNullable(payload).orElse(new byte[0]);
        boolean sent = errorChannel.send(
                MessageBuilder.withPayload(payloadNormalized)
                              .setHeader("component", "testsend-manager")
                              .setHeader("input-channel", inputChannelName)
                              .setHeader("message", message)
                              .setHeader("time", now.format(DateTimeFormatter.ISO_DATE_TIME))
                              .build());
        if (!sent) {
            log.warn("Unable to send an error message: Source {}: {}: Payload {}", inputChannelName, message, new String(payload, StandardCharsets.UTF_8));
        }
    }

    /**
     * Request start directly to Job-Manager.
     *
     * @param header  Header to send
     * @param lineJob Job start payload
     * @throws IOException When can't serialize or send the kick message.
     */
    public void sendJobManager(Map<String, Object> header, LineJob lineJob) throws IOException {
        log.info("Send job manager lineTask: Started: CampaignId: {}", lineJob.getInfo().getCampaignId());
        val payload = objectMapperWrapper.serializeToBytes(lineJob);
        val message = MessageBuilder.withPayload(payload)
                                    .copyHeaders(header)
                                    .build();
        sendMessage(outputChannels.sendJobManager(), message, "Could not send line job");
    }
}
