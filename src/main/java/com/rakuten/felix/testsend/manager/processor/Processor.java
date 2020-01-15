package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.messaging.MessageSender;
import com.rakuten.felix.testsend.manager.messaging.NotificationService;
import com.rakuten.felix.testsend.manager.messaging.dto.Header;
import com.rakuten.felix.testsend.manager.messaging.dto.JobStatus;
import com.rakuten.felix.testsend.manager.messaging.dto.ReplyJobStatusPayload;
import com.rakuten.felix.testsend.manager.serde.ObjectMapperWrapper;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.validator.Validator;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import com.rakuten.felix.testsend.manager.web.dto.KickTestSendRequest;
import com.rakuten.felix.testsend.manager.webclients.CampaignSchedulerService;
import com.rakuten.felix.testsend.manager.webclients.dto.JobManagerPayload;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJob;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
public class Processor {
    private final DataStoreService dataStore;
    private final MailContentBuilder mailContentBuilder;
    private final NotificationService notificationService;
    private final CampaignSchedulerService schedulerService;
    private final ObjectMapperWrapper objectMapperWrapper;
    private final MessageSender messageSender;
    private final ReplyConfig replyConfig;

    /**
     * Initialize the service.
     *
     * @param dataStore           Data store.
     * @param mailContentBuilder  Mail content builder.
     * @param notificationService Notification service.
     * @param schedulerService    Campaign scheduler service.
     * @param objectMapperWrapper Object mapper wrapper.
     * @param messageSender       Message sender.
     * @param replyConfig         Reply config.
     */
    public Processor(DataStoreService dataStore,
                     MailContentBuilder mailContentBuilder,
                     NotificationService notificationService,
                     CampaignSchedulerService schedulerService,
                     ObjectMapperWrapper objectMapperWrapper,
                     MessageSender messageSender,
                     ReplyConfig replyConfig) {

        this.dataStore = dataStore;
        this.mailContentBuilder = mailContentBuilder;
        this.notificationService = notificationService;
        this.schedulerService = schedulerService;
        this.objectMapperWrapper = objectMapperWrapper;
        this.messageSender = messageSender;
        this.replyConfig = replyConfig;
    }

    /**
     * Process for kicking mail test send.
     *
     * @param request Kick mail test send request.
     */
    public TestSendHistory processKickingTestSend(KickMailTestSendRequest request) throws ValidationException {
        val mailJob = objectMapperWrapper.deserializeToObject(request.getMailJob().toJSONString(), MailJob.class);
        Validator.validate(mailJob);

        val reserveDate = mailJob.getSchedules().get(0).getReserveDate();
        val schedulerResponse = schedulerService.registerSingle(reserveDate, request.getMailJob());

        val info = buildInfo(mailJob, request.getUser());
        return dataStore.createHistory(request.getBundleId(), request.getBundleType(), schedulerResponse.getJdkId(), info);
    }

    /**
     * Process for kicking line test send.
     *
     * @param request Kick mail test send request.
     */
    public void processKickingTestSend(KickTestSendRequest request) throws IOException, ValidationException {
        val jobManagerPayload = objectMapperWrapper.deserializeToObject(request.getJob().toJSONString(), JobManagerPayload.class);
        Validator.validate(jobManagerPayload);

        val info = Info.builder().user(request.getUser()).contents(request.getContents()).recipients(request.getRecipients()).build();
        val history = dataStore.createHistory(request.getBundleId(), request.getBundleType(), null, info);
        val replyHeader = Header.buildWithContentType(jobManagerPayload.getInfo().getLogId(), history.getId(), replyConfig.getJobStatusHandlingChannel());
        jobManagerPayload.toBuilder().replyHeader(replyHeader).replyDestination(replyConfig.getJobStatusHandlingChannel());
        messageSender.sendJobManager(replyHeader, jobManagerPayload);
    }

    private Info buildInfo(MailJob mailJob, User user) {
        val parts = mailJob.getParts();
        val schedule = mailJob.getSchedules().get(0);

        val subjects = mailContentBuilder.buildSubjectContents(schedule.getSubjects(), parts);
        val htmlContents = mailContentBuilder.buildHtmlContents(schedule.getContents(), parts);
        val textContents = mailContentBuilder.buildTextContents(schedule.getContents(), parts);

        return Info.builder()
                   .user(user)
                   .subjects(subjects)
                   .htmlContents(htmlContents)
                   .textContents(textContents)
                   .recipients(mailJob.getPrependAddresses())
                   .build();
    }

    /**
     * Process when mail test send finished.
     *
     * @param jobId Job id.
     */
    public void processMailTestSendFinished(Integer jobId) {
        try {
            dataStore.updateStatusToFinishedByJobId(jobId);

            val entity = dataStore.getHistoryByJobId(jobId);
            val bundleId = entity.getBundleId();
            val userId = Optional.ofNullable(entity.getInfo())
                                 .map(Info::getUser)
                                 .map(User::getUserId)
                                 .orElseGet(() -> {
                                     log.warn("User id doesn't exist in info: info={}: Wil notify to userId=0", entity.getInfo());
                                     return 0;
                                 });
            notificationService.publishSuccessNotification(bundleId, userId);
        } catch (HistoryNotFoundException e) {
            // FIXME Until completely migrate to use this API for test sending, keep exception which data is not found by job id as warning.
            log.warn("Could not update history on mail test send finished: jobId={}, {} :", jobId, e.getMessage());
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
            dataStore.updateErrorMessageAndStatusToErrorByJobId(jobId, errorMessage);

            val entity = dataStore.getHistoryByJobId(jobId);
            val bundleId = entity.getBundleId();
            val userId = Optional.ofNullable(entity.getInfo())
                                 .map(Info::getUser)
                                 .map(User::getUserId)
                                 .orElseGet(() -> {
                                     log.warn("User id doesn't exist in info: Wil notify to userId=0", entity.getInfo());
                                     return 0;
                                 });
            notificationService.publishErrorNotification(bundleId, userId);
        } catch (HistoryNotFoundException e) {
            // FIXME Until completely migrate to use this API for test sending, keep exception which data is not found by job id as warning.
            log.warn("Could not update history on test sending error: jobId={}: {} :", jobId, e.getMessage());
        } catch (Exception e) {
            handleError("Test send error", jobId, e);
        }
    }

    /**
     * Handle reply message
     *
     * @param payload Payload.
     * @param header  Header.
     */
    public void handleReplyMessage(Header header, byte[] payload) throws ValidationException {
        val jobStatus = objectMapperWrapper.deserializeToObject(payload, ReplyJobStatusPayload.class);
        Validator.validate(jobStatus);

        if (jobStatus.getStatus() == JobStatus.FINISHED) {
            log.info("Job finished:");
            dataStore.updateStatusToFinishedByTestId(header.getTestId());
        } else if (jobStatus.getStatus() == JobStatus.FAILED) {
            log.info("Job failed:");
            dataStore.updateErrorMessageAndStatusToErrorByTestId(header.getTestId(), jobStatus.getMessage());
        }
    }

    private void handleError(String method, Integer jobId, Throwable throwable) {
        dataStore.updateErrorMessageAndStatusToErrorByJobId(jobId, throwable.getMessage());
        throw new ProcessingException(method, jobId, throwable);
    }
}
