package com.rakuten.felix.testsend.manager.datastore;

import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

@Slf4j
@Service
public class DataStoreService {
    private final TestSendHistoryRepository repository;
    private final Clock clock;

    /**
     * Initialize the service.
     *
     * @param repository Repository.
     * @param clock      Clock.
     */
    @Autowired
    public DataStoreService(TestSendHistoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
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
                                    .started(ZonedDateTime.now(clock))
                                    .build();
        repository.saveAndFlush(entity);
        log.debug("Create history: {}", entity);
        return entity;
    }

    /**
     * Update job id.
     *
     * @param id           History id.
     * @param jobId        Job id.
     * @param subjects     Subjects.
     * @param htmlContents Html contents.
     * @param textContents Text contents.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateJob(Integer id, Integer jobId, List<String> subjects, List<String> htmlContents, List<String> textContents, List<String> recipients) {
        val entity = repository.findById(id).orElseThrow(() -> new HistoryNotFoundException("Get history by id", id));
        entity.setJobId(jobId);
        val info = entity.getInfo()
                .toBuilder()
                .subjects(subjects)
                .htmlContents(htmlContents)
                .textContents(textContents)
                .recipients(recipients)
                .build();
        entity.setInfo(info);
        log.debug("Update job id and info: id={}, jobId={}, info={}", id, jobId, info);
        repository.saveAndFlush(entity);
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
     * Update status to error by history id.
     *
     * @param id           History id.
     * @param errorMessage Error message.
     * @throws HistoryNotFoundException When data is not found.
     */
    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 1.5), include = Throwable.class, exclude = HistoryNotFoundException.class)
    public void updateErrorMessageAndStatusToErrorById(Integer id, String errorMessage) {
        val entity = repository.findById(id).orElseThrow(() -> new HistoryNotFoundException("Get history by id", id));
        val info = entity.getInfo().toBuilder().errorMessage(errorMessage).build();
        entity.setInfo(info);
        entity.setStatus(TestSendStatus.ERROR);
        entity.setFinished(ZonedDateTime.now(clock));
        log.debug("Update status to error: id={}, errorMessage={}", id, errorMessage);
        repository.saveAndFlush(entity);
    }
}
