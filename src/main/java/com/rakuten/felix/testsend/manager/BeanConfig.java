package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.common.VersionInfo;
import com.rakuten.felix.common.actuator.ActuatorConfig;
import com.rakuten.felix.common.web.security.service.api.ApiAuthConfig;
import com.rakuten.felix.common.web.security.service.api.ApiAuthService;
import com.rakuten.felix.common.web.utils.FelixAuthInterceptor;
import com.rakuten.felix.testsend.manager.webclients.testrecipient.TestRecipientClient;
import com.rakuten.felix.testsend.manager.webclients.workflow.WorkFlowClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

@Configuration
@Slf4j
public class BeanConfig {
    public static final ZoneId APPLICATION_TIME_ZONE_ID = ZoneId.of("JST", ZoneId.SHORT_IDS);
    public static final String NO_PROXY_REST_TEMPLATE = "noProxyRestTemplate";
    private static final String CONNECT_TIMEOUT = "${com.rakuten.felix.testsend-manager.proxy.connect-timeout}";
    private static final String READ_TIMEOUT = "${com.rakuten.felix.testsend-manager.proxy.read-timeout}";
    public static final String APP_NAME = "Test Send Manager";
    public static final String FELIX_API_REST_TEMPLATE = "felixApi";
    private static final String FELIX_PROXY_HOST = "${com.rakuten.felix.testsend-manager.proxy.host}";
    private static final String FELIX_PROXY_PORT = "${com.rakuten.felix.testsend-manager.proxy.port}";

    @Bean(name = FELIX_API_REST_TEMPLATE)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestTemplate felixRestTemplate(@Value(FELIX_PROXY_HOST) final String proxyHost,
                                          @Value(FELIX_PROXY_PORT) final Integer proxyPort,
                                          @Value(CONNECT_TIMEOUT) final Integer connectionTimeout,
                                          @Value(READ_TIMEOUT) final Integer readTimeout,
                                          FelixAuthInterceptor interceptor) {
        return buildRestTemplate(proxyHost, proxyPort, connectionTimeout, readTimeout, interceptor, "FELIX");
    }

    @Bean(name = NO_PROXY_REST_TEMPLATE)
    public RestTemplate noProxyRestTemplate(@Value(CONNECT_TIMEOUT) final Integer connectionTimeout,
                                            @Value(READ_TIMEOUT) final Integer readTimeout,
                                            FelixAuthInterceptor interceptor) {
        return buildRestTemplate(org.apache.commons.lang3.StringUtils.EMPTY, null, connectionTimeout, readTimeout, interceptor, "Push Campaign");
    }

    private RestTemplate buildRestTemplate(String proxyHost, Integer proxyPort, Integer connectTimeout, Integer readTimeout, FelixAuthInterceptor interceptor, String name) {
        val restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory;

        // PROXY Settings
        if (StringUtils.hasText(proxyHost) && Objects.nonNull(proxyPort)) {
            val httpClient = HttpClientBuilder.create()
                    // default schema is http
                    .setProxy(new HttpHost(proxyHost, proxyPort))
                    .build();
            requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        } else {
            requestFactory = new HttpComponentsClientHttpRequestFactory();
        }

        // Set timeout config (config is seconds)
        requestFactory.setConnectTimeout(connectTimeout * 1000);
        requestFactory.setReadTimeout(readTimeout * 1000);

        restTemplate.setRequestFactory(requestFactory);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        if (interceptor != null) {
            val interceptors = new ArrayList<>(restTemplate.getInterceptors());
            interceptors.add(interceptor);
            restTemplate.setInterceptors(interceptors);
        }
        log.info("REST template for {} API: proxyHost: {} proxyPort: {}", name, proxyHost, proxyPort);
        return restTemplate;
    }

    @Bean
    @ConfigurationProperties(prefix = "com.rakuten.felix.testsend-manager.test-recipients")
    public TestRecipientClient.TestRecipientProperties testRecipientProperties() {
        return new TestRecipientClient.TestRecipientProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "com.rakuten.felix.testsend-manager.workflow-client")
    public WorkFlowClient.WorkflowProperties workflowProperties() {
        return new WorkFlowClient.WorkflowProperties();
    }

    @Bean
    FelixAuthInterceptor felixAuthInterceptor() {
        return new FelixAuthInterceptor(APP_NAME);
    }

    @Bean
    @ConfigurationProperties(prefix = "com.rakuten.felix.application")
    public VersionInfo versionInfo() {
        return new VersionInfo();
    }


    @Bean
    public Clock clock() {
        return Clock.system(APPLICATION_TIME_ZONE_ID);
    }

    @Bean
    @ConfigurationProperties(prefix = "com.rakuten.felix.testsend-manager.actuator")
    public ActuatorConfig actuatorConfig() {
        return new ActuatorConfig();
    }

    @ConfigurationProperties(prefix = "com.rakuten.felix.testsend-manager.auth2")
    @Bean
    public ApiAuthConfig apiAuthConfig() {
        return new ApiAuthConfig();
    }

    @Bean
    public ApiAuthService apiAuthService(ApiAuthConfig config) {
        log.info("Load api authentication config: {}", config);
        return ApiAuthService.builder()
                .apiKey(config.getKey())
                .test(config.getTest())
                .build();
    }
}
