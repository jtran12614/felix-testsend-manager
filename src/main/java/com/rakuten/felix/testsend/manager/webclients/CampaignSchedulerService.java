package com.rakuten.felix.testsend.manager.webclients;

import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.validator.Validator;
import com.rakuten.felix.testsend.manager.webclients.dto.RegisterCampaignRequest;
import com.rakuten.felix.testsend.manager.webclients.dto.RegisterCampaignResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Collections;

@Service
@Slf4j
public class CampaignSchedulerService {
    private final String registerAndGetUrl;
    private final RestTemplate restTemplate;

    /**
     * Initialize the service.
     *
     * @param registerAndGetUrl Get campaign URL.
     * @param restTemplate      REST template.
     */
    public CampaignSchedulerService(@Value("${com.rakuten.felix.testsend-manager.campaign-scheduler.register-and-get-url}") String registerAndGetUrl,
                                    RestTemplate restTemplate) {

        this.registerAndGetUrl = registerAndGetUrl;
        this.restTemplate = restTemplate;
    }

    /**
     * Register and get campaign.
     */
    @Retryable(include = Throwable.class, exclude = RestClientException.class, backoff = @Backoff(multiplier = 2))
    public RegisterCampaignResponse registerSingle(ZonedDateTime reserveDate, JSONObject mailJob) throws ValidationException {
        log.debug("Register campaign: url={}, mailJobJson={}", registerAndGetUrl, mailJob);
        val request = new RegisterCampaignRequest(Collections.singletonList(reserveDate), mailJob);
        val response = restTemplate.postForObject(registerAndGetUrl, request, RegisterCampaignResponse.class);
        log.debug("Register campaign: jobId={}, response={}", response);
        Validator.validate(request);
        return response;
    }

}
