package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.MailContent;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class Processor {
    private static final Integer BUNDLE_TYPE_MAIL = 1;
    private final DataStoreService dataStore;
    private final MailContentBuilder mailContentBuilder;
    private final NotificationService notificationService;
    private final CampaignSchedulerService schedulerService;
    private final ObjectMapperWrapper objectMapperWrapper;
    private final MessageSender messageSender;
    private final ReplyConfig replyConfig;

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
        final Info info;
        if (BUNDLE_TYPE_MAIL.equals(request.getBundleType())) {
            info = buildMailInfo(mailJob, request.getUser());
        } else {
            // For scenario mail testing
            info = buildInfo(mailJob, request.getUser());
        }
        return dataStore.createHistory(request.getBundleId(), request.getBundleType(), schedulerResponse.getJdkId(), info);
    }

    /**
     * Process for kicking test send.
     *
     * @param request Kick test send request.
     */
    public TestSendHistory processKickingTestSend(KickTestSendRequest request) throws IOException, ValidationException {
        JobManagerPayload jobManagerPayload = objectMapperWrapper.deserializeToObject(request.getJob().toJSONString(), JobManagerPayload.class);
        Validator.validate(jobManagerPayload);

        val info = Info.builder().user(request.getUser()).contents(request.getContents()).recipients(request.getRecipients()).build();
        val history = dataStore.createHistory(request.getBundleId(), request.getBundleType(), null, info);
        val replyHeader = Header.buildWithContentType(jobManagerPayload.getInfo().getLogId(), history.getId(), replyConfig.getJobStatusHandlingChannel());
        jobManagerPayload = jobManagerPayload.toBuilder().replyHeader(replyHeader).replyDestination(replyConfig.getJobStatusHandlingChannel()).build();
        messageSender.sendJobManager(replyHeader, jobManagerPayload);
        return history;
    }

    private Info buildInfo(MailJob mailJob, User user) {
        val parts = mailJob.getParts();
        val columns = mailJob.getColumns();
        val schedule = mailJob.getSchedules().get(0);

        val replacements = mailContentBuilder.buildReplacements(columns, mailJob.getPermissionType());
        val subjects = mailContentBuilder.buildSubjectContents(schedule.getSubjects(), parts, replacements);
        val htmlContents = mailContentBuilder.buildHtmlContents(schedule.getContents(), parts, replacements);
        val textContents = mailContentBuilder.buildTextContents(schedule.getContents(), parts, replacements);

        return Info.builder()
                   .user(user)
                   .subjects(subjects)
                   .htmlContents(htmlContents)
                   .textContents(textContents)
                   .recipients(mailJob.getPrependAddresses())
                   .build();
    }

    private Info buildMailInfo(MailJob mailJob, User user) {
        val parts = mailJob.getParts();
        val columns = mailJob.getColumns();
        val replacements = mailContentBuilder.buildReplacements(columns, mailJob.getPermissionType());
        val contents = mailJob.getSchedules()
                              .stream()
                              .map(it -> {
                                  val subjects = mailContentBuilder.buildSubjectContents(it.getSubjects(), parts, replacements);
                                  val html = mailContentBuilder.buildHtmlContents(it.getContents(), parts, replacements);
                                  val text = mailContentBuilder.buildTextContents(it.getContents(), parts, replacements);
                                  return new MailContent(it.getId(), it.getDeviceCodes(), subjects, text, html);
                              })
                              .collect(Collectors.toList());
        val recipients = Optional.ofNullable(mailJob.getPrependAddresses())
                                 .map(it -> it.stream().distinct().collect(Collectors.toList()))
                                 .orElse(Collections.emptyList());
        return Info.builder()
                   .user(user)
                   .recipients(recipients)
                   .contents(contents)
                   .columns(mailJob.getColumns())
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
                                     log.warn("User id doesn't exist in info: Wil notify to userId=0, info={}", entity.getInfo());
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
        } else if(jobStatus.getStatus() == JobStatus.PROCESSING) {
            log.info("Job processing:");
        } else {
            log.info("Job failed:");
            dataStore.updateErrorMessageAndStatusToErrorByTestId(header.getTestId(), jobStatus.getMessage());
        }
    }

    private void handleError(String method, Integer jobId, Throwable throwable) {
        dataStore.updateErrorMessageAndStatusToErrorByJobId(jobId, throwable.getMessage());
        throw new ProcessingException(method, jobId, throwable);
    }
}
