package com.rakuten.felix.testsend.manager.web.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
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
    public EmbeddedServletContainerFactory servletContainer(
            @Value("${com.rakuten.felix.testsend-manager.tomcat.ajp.port}") int ajpPort,
            @Value("${com.rakuten.felix.testsend-manager.tomcat.ajp.enabled}") boolean tomcatAjpEnabled
    ) {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        if (tomcatAjpEnabled) {
            Connector ajpConnector = new Connector("AJP/1.3");
            ajpConnector.setPort(ajpPort);
            ajpConnector.setSecure(false);
            ajpConnector.setAllowTrace(false);
            ajpConnector.setRedirectPort(8443);
            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }
        return tomcat;
    }
}
