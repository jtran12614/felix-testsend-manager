package com.rakuten.felix.testsend.manager.web.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfiguration {
    /**
     * Create embedded servlet container factory.
     *
     * @return Bean.
     */
    @Bean
    public ServletWebServerFactory servletContainer(
            @Value("${com.rakuten.felix.testsend-manager.tomcat.ajp.port}") int ajpPort,
            @Value("${com.rakuten.felix.testsend-manager.tomcat.ajp.enabled}") boolean tomcatAjpEnabled
    ) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        if (tomcatAjpEnabled) {
            Connector ajpConnector = new Connector("AJP/1.3");
            ajpConnector.setPort(ajpPort);
            ajpConnector.setSecure(false);
            ajpConnector.setAllowTrace(false);
            ajpConnector.setRedirectPort(8443);
            // TODO: improve security
            // See https://github.com/spring-projects/spring-boot/issues/20377
            ((AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler()).setSecretRequired(false);
            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }
        return tomcat;
    }
}
