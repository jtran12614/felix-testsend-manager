package com.rakuten.felix.testsend.manager.web;

import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.web.dto.MessageOnly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle {@link MethodArgumentNotValidException}.
     *
     * @param exception MethodArgumentNotValidException.
     * @return Error message.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageOnly handleRequestValidationFailed(MethodArgumentNotValidException exception) {
        log.warn("{}", exception.getLocalizedMessage());
        return new MessageOnly(exception.getMessage());
    }

    /**
     * Handle {@link ValidationException}.
     *
     * @param exception ValidationException.
     * @return Error message.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageOnly handleRequestValidationFailed(ValidationException exception) {
        log.warn("{}", exception.getLocalizedMessage());
        return new MessageOnly(exception.getMessage());
    }

    /**
     * Handle {@link HistoryNotFoundException}.
     *
     * @param exception Exception.
     * @return Error message.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MessageOnly handleHistoryNotFound(HistoryNotFoundException exception) {
        log.warn("{}", exception.getMessage());
        return new MessageOnly(exception.getMessage());
    }

    /**
     * Handle {@link UnauthorizedException}.
     *
     * @param exception Exception.
     * @return Error message.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageOnly handleUnauthorized(UnauthorizedException exception) {
        log.warn("Auth failed: {}", exception.getMessage());
        return new MessageOnly("Auth failed: " + exception.getMessage());
    }

    /**
     * Handle error response when throw {@link Throwable};
     *
     * @param throwable Throwable.
     * @return Error message.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageOnly handleThrowable(Throwable throwable) {
        log.error("Unexpected error: {}", throwable.getMessage(), throwable);
        return new MessageOnly("Unexpected error: " + throwable.getMessage());
    }
}
