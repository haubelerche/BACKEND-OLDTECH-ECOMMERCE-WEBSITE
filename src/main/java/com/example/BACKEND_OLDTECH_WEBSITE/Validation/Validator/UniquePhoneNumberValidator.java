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
    public boolean isValid(String phone_number, ConstraintValidatorContext context) {
        try {
            if (phone_number == null) {
                return true;
            }
            return !userRepository.existsByPhoneNumber(phone_number);
        } catch (Exception e) {
            // Log the exception if you have a logger
            // logger.error("Error validating unique phone number: " + e.getMessage(), e);
            return false;
        }
    }
}