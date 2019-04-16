package com.apmats.rentflix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoCopyAvailableException extends RuntimeException {
    public NoCopyAvailableException(String message) {
        super(message);
    }

    public NoCopyAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
