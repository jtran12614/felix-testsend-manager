package com.rakuten.felix.testsend.manager.errorhandler;

import com.rakuten.felix.common.web.error.ErrorRestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerConfig extends ErrorRestControllerAdvice {

}
