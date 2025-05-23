package com.example.BACKEND_OLDTECH_WEBSITE.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to login or access resources while their account is suspended
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountSuspendedException extends RuntimeException {

    public AccountSuspendedException(String message) {
        super(message);
    }

    public AccountSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }
}
