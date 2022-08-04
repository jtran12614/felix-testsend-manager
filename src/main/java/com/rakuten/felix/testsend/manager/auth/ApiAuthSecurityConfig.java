package com.rakuten.felix.testsend.manager.auth;

import com.rakuten.felix.common.web.security.config.SecurityConfig;
import com.rakuten.felix.common.web.security.filter.AuthHeaderFilter;
import com.rakuten.felix.common.web.security.service.api.ApiAuthConfig;
import com.rakuten.felix.common.web.security.service.api.ApiAuthService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(0)
@Configuration
public class ApiAuthSecurityConfig extends SecurityConfig {

    public ApiAuthSecurityConfig(ApiAuthService authService,
                                 ApiAuthConfig config) {
        super(new AuthHeaderFilter(authService), config.getRoots());
    }
}
