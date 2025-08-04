package com.example.BACKEND_OLDTECH_WEBSITE.Exception;

public class ProductAlreadyInCartException extends RuntimeException {
    public ProductAlreadyInCartException(String message) {
        super(message);
    }
}
