package com.insurancesystem.Exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
