package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.jsonutils.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.messaging.NotificationService;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.webclients.JobDataKeeperService;
import lombok.val;
import org.springframework.stereotype.Service;

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
        dataStore.updateJobId(historyId, jobId);
    }

    /**
     * Process when mail test send finished.
     *
     * @param jobId      Job id.
     * @param scheduleId Schedule id.
     * @throws ValidationException When validation failed.
     */
    public void processMailTestSendFinished(Integer jobId, Integer scheduleId) throws ValidationException {
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

        val entity = dataStore.getHistoryByJobId(jobId);
        notificationService.publishSuccessNotification(jobId, entity.getBundleId());
    }

    /**
     * Process when mail test send is error.
     *
     * @param jobId        Job id.
     * @param errorMessage Error message.
     */
    public void processTestSendError(Integer jobId, String errorMessage) throws ValidationException {
        val mailJobWithContents = jobDataKeeperService.getMailJobWithContents(jobId);
        val info = Info.builder()
                .user(mailJobWithContents.getUser())
                .errorMessage(errorMessage)
                .build();
        val infoJson = objectMapper.serializeToString(info);
        dataStore.updateStatusToError(jobId, infoJson);

        val entity = dataStore.getHistoryById(jobId);
        notificationService.publishErrorNotification(jobId, entity.getBundleId());
    }

}
