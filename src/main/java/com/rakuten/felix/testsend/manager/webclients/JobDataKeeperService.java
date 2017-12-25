package com.rakuten.felix.testsend.manager.webclients;

import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.validator.Validator;
import com.rakuten.felix.testsend.manager.webclients.dto.JobIdWrapper;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJob;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class JobDataKeeperService {
    private final String getJobUrl;
    private final RestTemplate restTemplate;

    /**
     * Initialize the service.
     *
     * @param getJobUrl    Job Data Keeper get job URL.
     * @param restTemplate Job Data Keeper REST template.
     */
    public JobDataKeeperService(@Value("${com.rakuten.felix.testsend-manager.job-data-keeper.get-job-url}") String getJobUrl,
                                RestTemplate restTemplate) {

        this.getJobUrl = getJobUrl;
        this.restTemplate = restTemplate;
    }

    /**
     * Get mail job with contents.
     *
     * @param jobId Job id
     * @return Mail job object with contents.
     */
    @Retryable(include = Throwable.class, exclude = HttpResponseException.class, backoff = @Backoff(multiplier = 2))
    public MailJob getMailJob(Integer jobId) throws ValidationException {
        log.debug("Get mail job: url={}, jobId={}", getJobUrl, jobId);
        val jobIdWrapper = new JobIdWrapper(jobId);
        val mailJob = restTemplate.postForObject(getJobUrl, jobIdWrapper, MailJob.class);
        log.debug("Get mail job: jobId={}, response={}", jobId, mailJob);
        Validator.validate(mailJob);
        return mailJob;
    }

}
