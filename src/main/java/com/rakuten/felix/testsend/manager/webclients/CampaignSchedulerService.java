package com.rakuten.felix.testsend.manager.webclients;

import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.webclients.dto.RegisterCampaignRequest;
import com.rakuten.felix.testsend.manager.webclients.dto.RegisterCampaignResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampaignSchedulerService {

    @Value("${com.rakuten.felix.testsend-manager.campaign-scheduler.register-and-get-url}")
    private final String registerAndGetUrl;

    private final RestTemplate restTemplate;

    /**
     * Register and get campaign.
     */
    @Retryable(include = Throwable.class, exclude = {RestClientException.class, ValidationException.class}, backoff = @Backoff(multiplier = 2))
    public RegisterCampaignResponse registerSingle(ZonedDateTime reserveDate, Map<String, Object> mailJob) throws ValidationException {
        val request = new RegisterCampaignRequest(Collections.singletonList(reserveDate), mailJob);
        log.debug("Register campaign: url={}, request={}", registerAndGetUrl, request);
        val response = restTemplate.exchange(registerAndGetUrl, HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<List<RegisterCampaignResponse>>() {
                });
        log.debug("Register campaign: response={}", response);
        return getFirstElement(response);
    }

    private RegisterCampaignResponse getFirstElement(HttpEntity<List<RegisterCampaignResponse>> response) throws ValidationException {
        return Optional.ofNullable(response)
                       .map(it -> it.getBody())
                       .filter(it -> !it.isEmpty())
                       .map(it -> it.stream().findFirst().get())
                       .orElseThrow(() -> new ValidationException("Register campaign: response body cannot be null or empty: response=" + response));
    }
}
