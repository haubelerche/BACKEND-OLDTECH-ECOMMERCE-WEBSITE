package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        try {
            if (email == null) {
                return true; // xu lý 0
            }
            return !userRepository.existsByEmail(email);
        } catch (Exception e) {
            // Log the exception if you have a logger
            // logger.error("Error validating unique email: " + e.getMessage(), e);
            return false;
        }
    }
}