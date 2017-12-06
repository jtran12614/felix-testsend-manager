package com.rakuten.felix.testsend.manager.errorhandler;

import com.rakuten.felix.testsend.manager.messaging.MessageSender;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ErrorHandler {
    private final MessageSender messageSender;

    /**
     * Initialize service.
     *
     * @param messageSender Message sender.
     */
    public ErrorHandler(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Handle test job initialization exception.
     *
     * @param exception Exception.
     * @param payload   Payload.
     */
    public void handleExceptionWithPayload(Exception exception, byte[] payload, String inputChannelName) {
        val errorMessage = getErrorMessage(exception);
        messageSender.sendErrorMessage(payload, errorMessage, inputChannelName);
        log.error(errorMessage, exception);
    }

    private String getErrorMessage(Throwable throwable) {
        return getStreamBuilderWithErrorMessages(throwable, Stream.builder())
                .build()
                .filter(Objects::nonNull)
                .filter(it -> !it.isEmpty())
                .collect(Collectors.joining(": "));
    }

    /**
     * Don't use it. Use {@code getErrorMessages(throwable)} instead.
     *
     * @param throwable Throwable.
     * @param builder   Stream builder.
     * @return Stream builder with messages.
     */
    private Stream.Builder<String> getStreamBuilderWithErrorMessages(Throwable throwable, Stream.Builder<String> builder) {
        return Optional.ofNullable(throwable)
                .map(it -> {
                    builder.add(it.getMessage());
                    return getStreamBuilderWithErrorMessages(it.getCause(), builder);
                })
                .orElse(builder);
    }
}
