package com.rakuten.felix.testsend.manager.webclients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@Slf4j
class RestTemplateConfig {
    /**
     * Create REST template.
     *
     * @param proxyHost Proxy host.
     * @param proxyPort Proxy port.
     * @return Bean.
     */
    @Bean
    public RestTemplate restTemplate(@Value("${com.rakuten.felix.testsend-manager.proxy.host}") final String proxyHost,
                                     @Value("${com.rakuten.felix.testsend-manager.proxy.port}") final Integer proxyPort) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (StringUtils.hasText(proxyHost) && proxyPort != null) {
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
            log.info("REST template: HTTP proxy configured: {}", proxy);
        }
        final RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        log.info("REST template: configured");
        return restTemplate;
    }
}
