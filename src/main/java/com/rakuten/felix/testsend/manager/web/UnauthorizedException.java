package com.rakuten.felix.testsend.manager.web;

public class UnauthorizedException extends Exception {
    public UnauthorizedException() {
    }

    public UnauthorizedException(String ipAddress, String path) {
        super("Unauthorized: IP = " + ipAddress + ": Path = " + path);
    }
}
