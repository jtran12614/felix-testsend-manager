package com.rakuten.felix.testsend.manager.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller allows remote users to log errors and warnings. This can be used to test alert rules.
 * <p>
 * The following endpoints are supported:
 * - POST /logging/error:   Generate ERROR level log
 * - POST /logging/warning: Generate WARN level log
 * <p>
 * Both endpoints have an optional "message" parameter, which allows the user to customize the log message.
 * If the "message" parameter is not set, a default string is used.
 * <p>
 * Example usage:
 * <p>
 * curl -u 'xxx:yyy' --request POST http://[host:port]/logging/error
 * curl -u 'xxx:yyy' --request POST http://[host:port]/logging/warning
 * <p>
 * curl -u 'xxx:yyy' --request POST --data 'message=some log message' http://[host:port]/logging/error
 * curl -u 'xxx:yyy' --request POST --data 'message=some log message' http://[host:port]/logging/warning
 */

@Slf4j
@Controller
@RequestMapping("/logging")
public class ErrorLogController {

    @ResponseBody
    @PostMapping(value = "/error")
    public String logError(@RequestParam(name = "message", defaultValue = "TEST ERROR") String message) {
        log.error(message);
        return "OK";
    }

    @ResponseBody
    @PostMapping(value = "/warning")
    public String logWarning(@RequestParam(name = "message", defaultValue = "TEST WARNING") String message) {
        log.warn(message);
        return "OK";
    }
}
