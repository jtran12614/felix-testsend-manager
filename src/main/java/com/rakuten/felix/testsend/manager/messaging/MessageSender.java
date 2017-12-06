package com.rakuten.felix.testsend.manager.messaging;

import com.rakuten.felix.testsend.manager.jsonutils.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.messaging.dto.KickMailTestSendMessage;
import com.rakuten.felix.testsend.manager.messaging.dto.Notification;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@EnableBinding(OutputChannels.class)
public class MessageSender {
    private final OutputChannels outputChannels;
    private final InputChannels inputChannels;
    private final ObjectMapperWrapper objectMapperWrapper;

    private final Clock clock = Clock.systemDefaultZone();

    /**
     * Initialize service.
     *
     * @param outputChannels      Output channels.
     * @param inputChannels       Input channels.
     * @param objectMapperWrapper JSON object mapper wrapper.
     */
    @Autowired
    public MessageSender(OutputChannels outputChannels,
                         InputChannels inputChannels,
                         ObjectMapperWrapper objectMapperWrapper) {
        this.outputChannels = outputChannels;
        this.inputChannels = inputChannels;
        this.objectMapperWrapper = objectMapperWrapper;
    }


    /**
     * Send a message to kick test send channel.
     *
     * @param id          History id.
     * @param mailJobJson Mail job json.
     */
    public void kickTestSendMessage(Integer id, String mailJobJson) throws MessageSendException {
        val mailTestSendPayload = objectMapperWrapper.serializeToBytes(new KickMailTestSendMessage(id, mailJobJson));
        sendMessage(outputChannels.outKickTestSend(),
                MessageBuilder.withPayload(mailTestSendPayload)
                        .setReplyChannel(inputChannels.inKickTestSendFinished())
                        .build(),
                "Could not send kicking mail test send message");
    }

    /**
     * Send a message to publish notification channel.
     *
     * @param notification Notification
     */
    public void publishNotification(Notification notification) throws MessageSendException {
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
    private void sendMessage(MessageChannel channel, Message<?> message, CharSequence... errorMessages) throws MessageSendException {
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
}
