package com.apmats.rentflix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RecommendationComputationException extends RuntimeException {
    public RecommendationComputationException(String message) {
        super(message);
    }

    public RecommendationComputationException(String message, Throwable cause) {
        super(message, cause);
    }
}
