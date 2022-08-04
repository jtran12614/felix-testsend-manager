package com.rakuten.felix.testsend.manager.webclients.workflow;

import com.rakuten.felix.testsend.manager.BeanConfig;
import com.rakuten.felix.testsend.manager.webclients.workflow.dto.JobStartResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkFlowClient {

    private static final String ASYNC = "async";

    @Qualifier(BeanConfig.NO_PROXY_REST_TEMPLATE)
    private final RestTemplate restTemplate;
    private final WorkflowProperties properties;

    public JobStartResponse createWorkflow(Map<String, Object> workflow, Boolean async) {
        log.debug("Create workflow - Start: async={}", async);
        log.trace("Workflow payload = {}", workflow);
        val path = UriComponentsBuilder.fromHttpUrl(properties.endpoints.base).queryParam(ASYNC, async).toUriString();
        val response = restTemplate.postForObject(path, workflow, JobStartResponse.class);
        log.debug("Create workflow - Finish: response = {}", response);
        return response;
    }

    @Data
    public static class WorkflowProperties {
        public Endpoint endpoints;
    }

    @Data
    @NoArgsConstructor
    public static class Endpoint {
        public String base;
        public String action;
    }

}

