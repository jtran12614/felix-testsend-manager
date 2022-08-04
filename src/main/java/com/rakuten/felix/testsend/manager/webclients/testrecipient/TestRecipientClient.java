package com.rakuten.felix.testsend.manager.webclients.testrecipient;

import com.rakuten.felix.testsend.manager.BeanConfig;
import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.webclients.testrecipient.dto.TestRecipient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRecipientClient {

    private static final String HEADER_AUTHENTICATION = "Authentication";
    private static final String CLIENT_ID = "clientId";
    private static final String GROUP_ID = "groupId";

    private final TestRecipientProperties testRecipientProperties;

    @Qualifier(BeanConfig.NO_PROXY_REST_TEMPLATE)
    private final RestTemplate restTemplate;

    @Retryable(backoff = @Backoff(multiplier = 2), exclude = RestClientResponseException.class, include = Throwable.class)
    public List<TestRecipient> getTestRecipients(Integer felixClientId, Integer testRecipientGroupId) throws ValidationException {
        val httpHeaders = buildHttpHeaders(felixClientId);
        val httpEntity = new HttpEntity<>(httpHeaders);
        val url = UriComponentsBuilder.fromHttpUrl(testRecipientProperties.url)
                .queryParam(GROUP_ID, testRecipientGroupId)
                .build()
                .encode()
                .toUri();
        log.debug("TestRecipient Url: {}", url);
        val response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<List<TestRecipient>>() {
                }).getBody();
        return response;
    }

    private HttpHeaders buildHttpHeaders(Integer clientId) {
        val httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER_AUTHENTICATION, testRecipientProperties.auth);
        httpHeaders.set(CLIENT_ID, clientId.toString());
        return httpHeaders;
    }

    @Data
    @NoArgsConstructor
    public static class TestRecipientProperties {
        public String url;
        public String auth;
    }
}
