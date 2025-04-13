package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.AgeRestriction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.time.ZoneId;

@Component
public class AgeRestrictionValidator implements ConstraintValidator<AgeRestriction, Date> {

    @Override
    public boolean isValid(Date dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return false;
        }

        LocalDate birthDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();

        return age >= 18;
    }
}
