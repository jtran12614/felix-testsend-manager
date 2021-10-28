package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.jobmanager.dto.core.JobStartPayload;
import com.rakuten.felix.jobmanager.dto.core.JobStatus;
import com.rakuten.felix.jobmanager.dto.core.ReplyJobInfoPayload;
import com.rakuten.felix.testsend.manager.datastore.DataStoreService;
import com.rakuten.felix.testsend.manager.datastore.HistoryNotFoundException;
import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.MailContent;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.messaging.MessageSender;
import com.rakuten.felix.testsend.manager.messaging.NotificationService;
import com.rakuten.felix.testsend.manager.messaging.dto.Header;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.validator.Validator;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import com.rakuten.felix.testsend.manager.web.dto.KickTestSendRequest;
import com.rakuten.felix.testsend.manager.webclients.CampaignSchedulerService;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJob;
import com.rakuten.felix.testsend.manager.webclients.dto.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Processor {

    private static final Integer BUNDLE_TYPE_MAIL = 1;
    private static final String  LOG_ID           = "logId";

    private final DataStoreService         dataStore;
    private final MailContentBuilder       mailContentBuilder;
    private final NotificationService      notificationService;
    private final CampaignSchedulerService schedulerService;
    private final ObjectMapper             mapper;
    private final MessageSender            messageSender;
    private final ReplyConfig              replyConfig;

    /**
     * Process for kicking mail test send.
     *
     * @param request Kick mail test send request.
     */
    public TestSendHistory processKickingTestSend(KickMailTestSendRequest request) throws ValidationException {
        val mailJob = mapper.convertValue(request.getMailJob(), MailJob.class);
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
    public TestSendHistory processKickingTestSend(KickTestSendRequest request) throws ValidationException, JsonProcessingException {
        JobStartPayload jobManagerPayload = mapper.convertValue(request.getJob(), JobStartPayload.class);
        Validator.validate(jobManagerPayload);
        val info = Info.builder().user(request.getUser()).contents(request.getContents()).recipients(request.getRecipients()).build();
        val history = dataStore.createHistory(request.getBundleId(), request.getBundleType(), null, info);
        val logId = Optional.ofNullable(jobManagerPayload.getInfo().get(LOG_ID)).map(Object::toString).orElse(null);
        val replyHeader = Header.buildWithContentType(logId, history.getId(), replyConfig.getJobStatusHandlingChannel());
        jobManagerPayload = jobManagerPayload.toBuilder().replyHeader(replyHeader).replyDestination(replyConfig.getJobStatusHandlingChannel()).build();
        messageSender.sendJobManager(replyHeader, jobManagerPayload);
        return history;
    }

    private Info buildInfo(MailJob mailJob, User user) {
        val parts = mailJob.getParts();
        val columns = mailJob.getColumns();
        val schedule = mailJob.getSchedules().get(0);

        val replacements = mailContentBuilder.buildReplacements(columns);
        val subjects = mailContentBuilder.buildSubjectContents(schedule.getSubjects(), parts, replacements, mailJob.getPermissionType());
        val htmlContents = mailContentBuilder.buildHtmlContents(schedule.getContents(), parts, replacements, mailJob.getPermissionType());
        val textContents = mailContentBuilder.buildTextContents(schedule.getContents(), parts, replacements, mailJob.getPermissionType());

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
        val replacements = mailContentBuilder.buildReplacements(columns);
        val contents = mailJob.getSchedules()
                              .stream()
                              .map(it -> {
                                  val subjects = mailContentBuilder.buildSubjectContents(it.getSubjects(), parts, replacements, mailJob.getPermissionType());
                                  val html = mailContentBuilder.buildHtmlContents(it.getContents(), parts, replacements, mailJob.getPermissionType());
                                  val text = mailContentBuilder.buildTextContents(it.getContents(), parts, replacements, mailJob.getPermissionType());
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
        final ReplyJobInfoPayload jobStatus;
        try {
            jobStatus = mapper.readValue(payload, ReplyJobInfoPayload.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't deserialize to payload: json=" + new String(payload, StandardCharsets.UTF_8), e);
        }
        Validator.validate(jobStatus);
        if (jobStatus.getStatus() == JobStatus.FINISHED) {
            log.info("Job finished:");
            dataStore.updateStatusToFinishedByTestId(header.getTestId());
        } else if (jobStatus.getStatus() == JobStatus.PROCESSING) {
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
