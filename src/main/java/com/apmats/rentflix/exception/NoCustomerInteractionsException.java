package com.apmats.rentflix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoCustomerInteractionsException extends RuntimeException {
    public NoCustomerInteractionsException(String message) {
        super(message);
    }

    public NoCustomerInteractionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
