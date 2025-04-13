package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepo;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null) {
            return true;
        }
        return !userRepo.existsByUsername(username);
    }
}