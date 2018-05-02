package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.datastore.TestSendHistoryRepository;
import com.rakuten.felix.testsend.manager.messaging.MessageSender;
import com.rakuten.felix.testsend.manager.messaging.OutputChannels;
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
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

import java.io.IOException;
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
    void setUp() throws IOException {
        initMocks(this);
        val dataStore = new DataStoreService(repository, clock);
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
                new ObjectMapperWrapper(),
                "prefix.",
                "destination",
                clock);
        controller = new WebController(dataStore, messageSender);
        versionInfo = new VersionInfo();
        authConfiguration = new AuthConfiguration(new AuthInterceptor("auth-key", true));
    }

    @Test
    void getBuildStartTimeAsFormattedStringTest() throws IOException {
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
        assertNotNull(new GlobalExceptionHandler().handleHistoryNotFound(new MethodArgumentNotValidException(parameter, errors)));
        assertNotNull(new GlobalExceptionHandler().handleHistoryNotFound(new HistoryNotFoundException("Not found.", 1)));
        assertNotNull(new GlobalExceptionHandler().handleUnauthorized(new UnauthorizedException("128.0.0.1", "dummy/path")));
        assertNotNull(new GlobalExceptionHandler().handleThrowable(new Throwable()));
    }

    @Test
    void authConfigurationTest() throws IOException {
        authConfiguration.addInterceptors(new InterceptorRegistry());
    }
}
