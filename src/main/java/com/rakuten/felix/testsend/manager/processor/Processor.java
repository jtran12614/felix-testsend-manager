package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.jsonutils.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.messaging.NotificationService;
import com.rakuten.felix.testsend.manager.webclients.JobDataKeeperService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class Processor {
    private final DataStoreService dataStore;
    private final ObjectMapperWrapper objectMapper;
    private final JobDataKeeperService jobDataKeeperService;
    private final MailContentBuilder mailContentBuilder;
    private final NotificationService notificationService;

    /**
     * Initialize the service.
     *
     * @param dataStore            Data store.
     * @param objectMapper         Object mapper wrapper.
     * @param jobDataKeeperService Job data keeper service.
     * @param mailContentBuilder   Mail content builder.
     * @param notificationService  Notification service.
     */
    public Processor(DataStoreService dataStore,
                     ObjectMapperWrapper objectMapper,
                     JobDataKeeperService jobDataKeeperService,
                     MailContentBuilder mailContentBuilder,
                     NotificationService notificationService) {
        this.dataStore = dataStore;
        this.objectMapper = objectMapper;
        this.jobDataKeeperService = jobDataKeeperService;
        this.mailContentBuilder = mailContentBuilder;
        this.notificationService = notificationService;
    }

    /**
     * Process when mail test send is error.
     *
     * @param historyId History id.
     * @param jobId     Job id.
     */
    public void processKickingTestSendFinished(Integer historyId, Integer jobId) {
        try {
            if (Objects.isNull(jobId)) {
                val info = Info.builder()
                        .errorMessage("Job initialization failed")
                        .build();
                val infoJson = objectMapper.serializeToString(info);
                dataStore.updateInfoAndStatusToErrorById(historyId, infoJson);
            } else {
                dataStore.updateJobId(historyId, jobId);
            }
        } catch (Exception e) {
            handleError("Kicking test send finished", jobId, e);
        }
    }

    /**
     * Process when mail test send finished.
     *
     * @param jobId      Job id.
     * @param scheduleId Schedule id.
     */
    public void processMailTestSendFinished(Integer jobId, Integer scheduleId) {
        try {
            val mailJobWithContents = jobDataKeeperService.getMailJobWithContents(jobId);
            val schedule = mailJobWithContents.getSchedules().get(scheduleId);
            val parts = mailJobWithContents.getParts();

            val subjects = mailContentBuilder.buildSubjectContents(schedule.getSubjects(), parts);
            val htmlContents = mailContentBuilder.buildHtmlContents(schedule.getContents(), parts);
            val textContents = mailContentBuilder.buildTextContents(schedule.getContents(), parts);
            val info = Info.builder()
                    .subjects(subjects)
                    .htmlContents(htmlContents)
                    .textContents(textContents)
                    .user(mailJobWithContents.getUser())
                    .build();
            val infoJson = objectMapper.serializeToString(info);
            dataStore.updateStatusToFinished(jobId, infoJson);

            val bundleId = dataStore.getHistoryByJobId(jobId).getBundleId();
            val userId = mailJobWithContents.getUser().getUserId();
            notificationService.publishSuccessNotification(bundleId, userId);
        } catch (HistoryNotFoundException e) {
            // FIXME Until completely migrate to use this API for test sending, keep exception which data is not found by job id as warning.
            log.warn("Could not update history on mail test send finished: jobId={}, scheduleId={}: {} :", jobId, scheduleId, e.getMessage());
        } catch (Exception e) {
            handleError("Mail test send finished", jobId, e);
        }
    }

    /**
     * Process when mail test send is error.
     *
     * @param jobId        Job id.
     * @param errorMessage Error message.
     */
    public void processTestSendError(Integer jobId, String errorMessage) {
        try {
            val mailJobWithContents = jobDataKeeperService.getMailJobWithContents(jobId);
            val info = Info.builder()
                    .user(mailJobWithContents.getUser())
                    .errorMessage(errorMessage)
                    .build();
            val infoJson = objectMapper.serializeToString(info);
            dataStore.updateStatusToErrorByJobIdAndInfo(jobId, infoJson);

            val bundleId = dataStore.getHistoryByJobId(jobId).getBundleId();
            val userId = mailJobWithContents.getUser().getUserId();
            notificationService.publishErrorNotification(bundleId, userId);
        } catch (HistoryNotFoundException e) {
            // FIXME Until completely migrate to use this API for test sending, keep exception which data is not found by job id as warning.
            log.warn("Could not update history on test sending error: jobId={}: {} :", jobId, e.getMessage());
        } catch (Exception e) {
            handleError("Test send error", jobId, e);
        }
    }

    private void handleError(String name, Integer jobId, Throwable throwable) {
        dataStore.updateStatusToErrorByJobId(jobId);
        throw new ProcessingException(name, jobId, throwable);
    }
}
