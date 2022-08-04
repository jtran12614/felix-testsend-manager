package com.rakuten.felix.testsend.manager.web.api;

import com.rakuten.felix.common.web.security.service.WebSession;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.web.dto.TestSendResponse;
import com.rakuten.felix.testsend.manager.web.dto.TriggerTestSendRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class Controller {

    private final Processor processor;

    @PostMapping(value = "/v1/test-send/trigger")
    public TestSendResponse triggerTestSend(@RequestBody @Valid TriggerTestSendRequest request, @AuthenticationPrincipal WebSession session)
            throws IOException, ValidationException {
        log.info("Trigger test send started: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        val history = processor.processTriggeringTestSend(request, session);
        log.info("Trigger test send finished: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        return TestSendResponse.fromEntity(history);
    }
}
