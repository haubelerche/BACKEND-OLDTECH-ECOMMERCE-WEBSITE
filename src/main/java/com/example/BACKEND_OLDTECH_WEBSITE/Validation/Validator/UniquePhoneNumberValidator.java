package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniquePhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UniquePhoneNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        try {
            // Null values should be validated with @NotNull
            if (phoneNumber == null) {
                return true;
            }

            // Skip validation for placeholder phone numbers used during OAuth registration
            if (phoneNumber.startsWith("placeholder_")) {
                return true;
            }

            // Check if the phone number exists in the database
            return !userRepository.existsByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            // Log the exception if you have a logger
            // logger.error("Error validating unique phone number: " + e.getMessage(), e);
            return false;
        }
    }
}

