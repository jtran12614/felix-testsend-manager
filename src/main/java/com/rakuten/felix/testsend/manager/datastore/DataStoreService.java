package com.rakuten.felix.testsend.manager.datastore;


import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DataStoreService {
    private final TestSendHistoryRepository repository;

    /**
     * Initialize the service.
     *
     * @param repository Repository.
     */
    @Autowired
    public DataStoreService(TestSendHistoryRepository repository) {
        this.repository = repository;
    }

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
        return repository.findById(id).orElseThrow(() -> new HistoryNotFoundException(id));
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
        return repository.findByJobId(jobId).orElseThrow(() -> new HistoryNotFoundException(jobId));
    }

    /**
     * Get histories by bundle id and bundle type.
     *
     * @param bundleId   Bundle id.
     * @param bundleType Bundle type.
     * @return List of test send history.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5))
    public List<TestSendHistory> getHistoriesByBundleIdAndType(Integer bundleId, Integer bundleType) {
        return repository.findByBundleIdAndBundleType(bundleId, bundleType);
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
    public TestSendHistory createHistory(Integer bundleId, Integer bundleType) {
        val entity = TestSendHistory.builder()
                .bundleId(bundleId)
                .bundleType(bundleType)
                .status(TestSendStatus.NEW)
                .build();
        repository.saveAndFlush(entity);
        log.debug("Create history: {}", entity);
        return entity;
    }

    /**
     * Update job id.
     *
     * @param id History id.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateJobId(Integer id, Integer jobId) {
        val rowAffected = repository.updateJobId(id, jobId);
        log.debug("Update job id: id={}, jobId={}, affectedRow={}", id, jobId, rowAffected);
        if (rowAffected < 1) {
            throw new HistoryNotFoundException(id);
        }
    }

    /**
     * Update status to finished.
     *
     * @param jobId History id.
     * @param info  Info json string.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateStatusToFinished(Integer jobId, String info) {
        val rowAffected = repository.updateInfoAndStatusFinished(jobId, info);
        log.debug("Update status to finished: jobId={}, info={}, affectedRow={}", jobId, info, rowAffected);
        if (rowAffected < 1) {
            throw new HistoryNotFoundException(jobId);
        }
    }

    /**
     * Update status to finished.
     *
     * @param jobId History id.
     * @param info  Info json string.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateStatusToError(Integer jobId, String info) {
        val rowAffected = repository.updateInfoAndStatusError(jobId, info);
        log.debug("Update status to error: jobId={}, info={}, affectedRow={}", jobId, info, rowAffected);
        if (rowAffected < 1) {
            throw new HistoryNotFoundException(jobId);
        }
    }
}
