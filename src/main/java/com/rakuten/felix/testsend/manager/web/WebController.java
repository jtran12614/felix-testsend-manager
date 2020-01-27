package com.rakuten.felix.testsend.manager.web;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.processor.Processor;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import com.rakuten.felix.testsend.manager.web.dto.KickTestSendRequest;
import com.rakuten.felix.testsend.manager.web.dto.TestSendResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/testsend-manager")
@CrossOrigin(origins = "*")
public class WebController {
    private final DataStoreService dataStore;
    private final Processor processor;

    /**
     * Initialize the controller.
     *
     * @param dataStore Data store.
     * @param processor Processor.
     */
    @Autowired
    public WebController(DataStoreService dataStore, Processor processor) {
        this.dataStore = dataStore;
        this.processor = processor;
    }

    /**
     * Handle request for getting a history.
     *
     * @return Response.
     */
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
     * @return Response.
     */
    @GetMapping(value = "/get-all")
    public Page<TestSendHistory> getAll(@RequestParam(value = "bundleId") Integer bundleId,
                                        @RequestParam(value = "bundleType") Integer bundleType,
                                        Pageable pageable) {
        log.debug("Get histories by bundleId={}, bundleType={}, pageInfo={}", bundleId, bundleType, pageable);
        return dataStore.getHistoriesByBundleIdAndType(bundleId, bundleType, pageable);
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

        log.debug("Kick mail test send request received: requestBody={}", request);
        val history = processor.processKickingTestSend(request);
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

        log.debug("Kick test send request received: requestBody={}", request);
        val history = processor.processKickingTestSend(request);
        return TestSendResponse.fromEntity(history);
    }
}
