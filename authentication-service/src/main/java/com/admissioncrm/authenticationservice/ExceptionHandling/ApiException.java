package com.admissioncrm.authenticationservice.ExceptionHandling;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
