package com.rakuten.felix.testsend.manager.processor;

class ProcessingException extends RuntimeException {
    ProcessingException(String message, Integer jobId, Throwable throwable) {
        super(String.format("Processing failed: %s : jobId=%d", message, jobId), throwable);
    }
}
