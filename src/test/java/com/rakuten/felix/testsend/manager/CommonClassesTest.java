package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.web.GlobalExceptionHandler;
import com.rakuten.felix.testsend.manager.web.UnauthorizedException;
import com.rakuten.felix.testsend.manager.web.WebController;
import com.rakuten.felix.testsend.manager.web.config.AuthConfiguration;
import com.rakuten.felix.testsend.manager.web.config.AuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.MessageChannel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

@Slf4j
class CommonClassesTest {

    private VersionInfo versionInfo;
    private AuthConfiguration authConfiguration;
    private WebController controller;

    @Mock
    private TestSendHistoryRepository repository;

    @Mock
    private MessageChannel kickTestSend;

    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        initMocks(this);
        val dataStore = new DataStoreService(repository, clock);
        val processor = new Processor(dataStore, null, null, null, new ObjectMapperWrapper());
        controller = new WebController(dataStore, processor);
        versionInfo = new VersionInfo();
        authConfiguration = new AuthConfiguration(new AuthInterceptor("auth-key", true));
    }

    @Test
    void getBuildStartTimeAsFormattedStringTest() {
        val response = versionInfo.getBuildStartTimeAsFormattedString();
        assertEquals("n/a", response);
    }

    @Test
    void exceptionTest() {
        val exception = new UnauthorizedException("128.0.0.1", "dummy/path");
        assertEquals("Unauthorized: IP = 128.0.0.1: Path = dummy/path", exception.getMessage());

    }

    @Test
    void webControllerExceptionHandlerTest() throws NoSuchMethodException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(controller, "testBean");
        MethodParameter parameter = new MethodParameter(controller.getClass().getMethod("get", Integer.class), 0);
        assertNotNull(new GlobalExceptionHandler().handleRequestValidationFailed(new MethodArgumentNotValidException(parameter, errors)));
        assertNotNull(new GlobalExceptionHandler().handleHistoryNotFound(new HistoryNotFoundException("Not found.", 1)));
        assertNotNull(new GlobalExceptionHandler().handleUnauthorized(new UnauthorizedException("128.0.0.1", "dummy/path")));
        assertNotNull(new GlobalExceptionHandler().handleThrowable(new Throwable()));
        assertNotNull(new GlobalExceptionHandler().handleValidationFailed(new ValidationException("Validation failed")));
    }

    @Test
    void authConfigurationTest() {
        authConfiguration.addInterceptors(new InterceptorRegistry());
    }
}
