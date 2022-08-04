package com.rakuten.felix.testsend.manager.datastore;

import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.time.Clock;
import java.time.ZonedDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class DataStoreService {
    private final TestSendHistoryRepository repository;
    private final Clock clock;
    private final TimeUtils timeUtils;

    /**
     * Get a history by id.
     *
     * @param id History id.
     * @return Test send history.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public TestSendHistory getHistoryById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new HistoryNotFoundException("Get history by id", id));
    }

    /**
     * Get a history by job id.
     *
     * @param jobId Job id.
     * @return List of test send history.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public TestSendHistory getHistoryByJobId(Integer jobId) {
        return repository.findByJobId(jobId).orElseThrow(() -> new HistoryNotFoundException("Get history by jobId", jobId));
    }

    /**
     * Get histories by bundle id and bundle type.
     *
     * @param bundleId   Bundle id.
     * @param bundleType Bundle type.
     * @param pageable   Pageable.
     * @return List of test send history.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5))
    public Page<TestSendHistory> getHistoriesByBundleIdAndType(Integer bundleId, Integer bundleType, Pageable pageable) {
        return repository.findByBundleIdAndBundleType(bundleId, bundleType, pageable);
    }

    /**
     * Create test send history.
     *
     * @param bundleId   Bundle id.
     * @param bundleType Bundle type.
     * @return Test send history.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5))
    public TestSendHistory createHistory(Integer bundleId, Integer bundleType, Integer jobId, Info info) {
        val entity = TestSendHistory.builder()
                                    .bundleId(bundleId)
                                    .bundleType(bundleType)
                                    .jobId(jobId)
                                    .info(info)
                                    .status(TestSendStatus.NEW)
                                    .started(timeUtils.getCurrentTime(clock))
                                    .build();
        repository.saveAndFlush(entity);
        log.debug("Create history: {}", entity);
        return entity;
    }

    /**
     * Update status to finished.
     *
     * @param jobId Job id.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateStatusToFinishedByJobId(Integer jobId) {
        val entity = repository.findByJobId(jobId).orElseThrow(() -> new HistoryNotFoundException("Get history by job id", jobId));
        entity.setStatus(TestSendStatus.FINISHED);
        entity.setFinished(ZonedDateTime.now(clock));
        log.debug("Update status to finished: jobId={}", jobId);
        repository.saveAndFlush(entity);
    }

    /**
     * Update status to finished.
     *
     * @param testId Test id.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateStatusToFinishedByTestId(Integer testId) {
        val entity = repository.findById(testId).orElseThrow(() -> new HistoryNotFoundException("Get history by test id", testId));
        entity.setStatus(TestSendStatus.FINISHED);
        entity.setFinished(ZonedDateTime.now(clock));
        log.debug("Update status to finished: testId={}", testId);
        repository.saveAndFlush(entity);
    }

    /**
     * Update status to error by job id.
     *
     * @param jobId        Job id.
     * @param errorMessage Error message.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateErrorMessageAndStatusToErrorByJobId(Integer jobId, String errorMessage) {
        val entity = repository.findByJobId(jobId).orElseThrow(() -> new HistoryNotFoundException("Get history by job id", jobId));
        val info = entity.getInfo().toBuilder().errorMessage(errorMessage).build();
        entity.setInfo(info);
        entity.setStatus(TestSendStatus.ERROR);
        entity.setFinished(ZonedDateTime.now(clock));
        log.debug("Update status to error: jobId={}, errorMessage={}", jobId, errorMessage);
        repository.saveAndFlush(entity);
    }

    /**
     * Update status to error by test id.
     *
     * @param testId        Test id.
     * @param errorMessage Error message.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateErrorMessageAndStatusToErrorByTestId(Integer testId, String errorMessage) {
        val entity = repository.findById(testId).orElseThrow(() -> new HistoryNotFoundException("Get history by test id", testId));
        val info = entity.getInfo().toBuilder().errorMessage(errorMessage).build();
        entity.setInfo(info);
        entity.setStatus(TestSendStatus.ERROR);
        entity.setFinished(ZonedDateTime.now(clock));
        log.debug("Update status to error: testId={}, errorMessage={}", testId, errorMessage);
        repository.saveAndFlush(entity);
    }
}
