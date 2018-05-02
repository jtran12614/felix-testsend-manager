package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.web.config.TomcatConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationTest {
    private TomcatConfiguration tomcatConfiguration;

    @BeforeEach
    void setUp() throws IOException {
        tomcatConfiguration = new TomcatConfiguration();
    }

    @Test
    void authTest() {
        assertNotNull(tomcatConfiguration.servletContainer(12345, false));
        assertNotNull(tomcatConfiguration.servletContainer(12345, true));
    }
}