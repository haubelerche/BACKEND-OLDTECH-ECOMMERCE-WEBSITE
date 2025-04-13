package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepo;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniquePhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String phone_number, ConstraintValidatorContext context) {
        if (phone_number == null) {
            return true;
        }
        return !userRepo.existsByPhoneNumber(phone_number);
    }
}