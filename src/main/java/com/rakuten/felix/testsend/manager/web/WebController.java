package com.rakuten.felix.testsend.manager.web;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.web.dto.HistoryDto;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import com.rakuten.felix.testsend.manager.web.dto.KickTestSendRequest;
import com.rakuten.felix.testsend.manager.web.dto.TestSendHistoryInitializeRequest;
import com.rakuten.felix.testsend.manager.web.dto.TestSendHistoryInitializeResponse;
import com.rakuten.felix.testsend.manager.web.dto.TestSendResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/testsend-manager")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class WebController {
    private final DataStoreService dataStore;
    private final Processor processor;

    /**
     * Handle request for getting a history.
     *
     * @deprecated Use /history/{id} instead.
     * @return Response.
     */
    @Deprecated
    @GetMapping(value = "/get/{id}")
    public TestSendHistory get(@PathVariable("id") Integer id) {
        log.debug("Get history by id={}", id);
        return dataStore.getHistoryById(id);
    }

    /**
     * Handle request for getting histories.
     *
     * @return Response.
     */
    @GetMapping(value = "/get-by-job-id/{id}")
    public TestSendHistory getBydJobId(@PathVariable("id") Integer id) {
        log.debug("Get history by job id={}", id);
        return dataStore.getHistoryByJobId(id);
    }

    /**
     * Handle request for getting histories.
     *
     * @deprecated Please use /histories instead. This list includes info column.
     * @return Response.
     */
    @Deprecated
    @GetMapping(value = "/get-all")
    public Page<TestSendHistory> getAll(@RequestParam(value = "bundleId") Integer bundleId,
                                        @RequestParam(value = "bundleType") Integer bundleType,
                                        Pageable pageable) {
        log.debug("Get histories by bundleId={}, bundleType={}, pageInfo={}", bundleId, bundleType, pageable);
        return dataStore.getHistoriesByBundleIdAndType(bundleId, bundleType, pageable);
    }

    /**
     * Initialize test send history.
     */
    @PostMapping("/api/v1/histories")
    @ResponseStatus(HttpStatus.CREATED)
    public TestSendHistoryInitializeResponse initializeTestSendHistory(@RequestBody @Valid TestSendHistoryInitializeRequest request) {
        log.info("Initializing test send history: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        log.debug("TestSendHistoryInitializeRequest={}", request);
        val response = processor.initializeTestSendHistory(request);
        log.info("Initializing test send history completed");
        return response;
    }

    /**
     * Handle request for kicking test mail sending
     *
     * @param request Request body.
     * @return Response.
     */
    @PostMapping(value = "/kick-mail-test-send")
    public TestSendResponse kickTestSend(@RequestBody @Valid KickMailTestSendRequest request)
            throws ValidationException {

        log.info("Kick test MAIL send started: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        log.debug("RequestBody={}", request);
        val history = processor.processKickingTestSend(request);
        log.info("Kick test MAIL send finished: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        return TestSendResponse.fromEntity(history);
    }

    /**
     * Handle request for kicking test line sending
     *
     * @param request Request body.
     * @return Response.
     */
    @PostMapping(value = "/kick-test-send")
    public TestSendResponse kickLineTestSend(@RequestBody @Valid KickTestSendRequest request)
            throws IOException, ValidationException {

        log.info("Kick test send started: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        log.debug("RequestBody={}", request);
        val history = processor.processKickingTestSend(request);
        log.info("Kick test send finished: bundleId={}, bundleType={}", request.getBundleId(), request.getBundleType());
        return TestSendResponse.fromEntity(history);
    }

    /**
     * Handle request for getting histories.
     * This response does not include info column.
     *
     * @return Response.
     */
    @GetMapping(value = "/histories")
    public Page<HistoryDto> getHistories(@RequestParam(value = "bundleId") Integer bundleId,
                                         @RequestParam(value = "bundleType") Integer bundleType,
                                         Pageable pageable) {
        log.info("Get histories started: bundleId={}, bundleType={}, pageInfo={}", bundleId, bundleType, pageable);
        val response = dataStore.getHistoriesByBundleIdAndType(bundleId, bundleType, pageable)
                                .map(HistoryDto::buildFromEntity);
        log.info("Get histories finished: bundleId={}, bundleType={}, total={}", bundleId, bundleType, response.getSize());
        return response;
    }

    /**
     * Handle request for getting a history.
     *
     * @return Response.
     */
    @GetMapping(value = "/histories/{id}")
    public TestSendHistory getHistory(@PathVariable("id") Integer id) {
        log.info("Get history started: id={}", id);
        val response = dataStore.getHistoryById(id);
        log.info("Get history finished: id={}", id);
        return response;
    }
}
