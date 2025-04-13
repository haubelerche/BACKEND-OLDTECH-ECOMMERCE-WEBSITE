package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepo;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true; // xu lý 0
        }
        return !userRepo.existsByEmail(email);
    }
}