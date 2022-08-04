package com.rakuten.felix.testsend.manager.config;

import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.ClearSystemProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ClearSystemProperty.ClearSystemProperties({
        @ClearSystemProperty(key = "http.proxyHost"),
        @ClearSystemProperty(key = "https.proxyHost")
})
@ClearEnvironmentVariable.ClearEnvironmentVariables({
        @ClearEnvironmentVariable(key = "HTTP_PROXY"),
        @ClearEnvironmentVariable(key = "HTTPS_PROXY")
})
public @interface StorageTestConfig {
}
