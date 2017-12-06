package com.rakuten.felix.testsend.manager.web.config;

import com.rakuten.felix.testsend.manager.web.UnauthorizedException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@ToString
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final String authenticationKey;
    private final boolean authenticationEnabled;

    /**
     * Initialize an interceptor.
     *
     * @param authenticationKey     Authentication key.
     * @param authenticationEnabled Authentication enabled.
     */
    public AuthInterceptor(@Value("${com.rakuten.felix.testsend-manager.auth.key}") String authenticationKey,
                           @Value("${com.rakuten.felix.testsend-manager.auth.enabled}") Boolean authenticationEnabled) {
        this.authenticationKey = authenticationKey;
        this.authenticationEnabled = authenticationEnabled;
    }

    private static String getRemoteAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null) {
            ipAddress = ipAddress.replaceFirst(",.*", "");  // cares only about the first IP if there is a list
        } else {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        val requestAuthenticationKey = request.getHeader("Authentication");
        if (!authenticationEnabled) {
            return true;
        } else if (Objects.equals(authenticationKey, requestAuthenticationKey)) {
            return true;
        } else {
            val ipAddress = getRemoteAddress(request);
            String path = request.getContextPath();
            log.warn("Authentication failed: Source {}: Path {}", ipAddress, path);
            throw new UnauthorizedException(ipAddress, path);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
