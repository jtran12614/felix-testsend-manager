package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.common.VersionInfo;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class TestConf {
    @MockBean
    VersionInfo versionInfo;
}
