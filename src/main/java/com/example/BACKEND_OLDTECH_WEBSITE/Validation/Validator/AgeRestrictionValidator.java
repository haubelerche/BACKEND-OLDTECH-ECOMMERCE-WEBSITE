package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.AgeRestriction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

@Component
public class AgeRestrictionValidator implements ConstraintValidator<AgeRestriction, Date> {

    @Override
    public void initialize(AgeRestriction constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Date dateOfBirth, ConstraintValidatorContext context) {
        try {
            // Null values should be validated with @NotNull
            if (dateOfBirth == null) {
                return true;
            }

            // Convert Date to LocalDate
            LocalDate birthDate = dateOfBirth.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate now = LocalDate.now();

            // Calculate age
            int age = Period.between(birthDate, now).getYears();

            // Check if user is at least 18 years old
            return age >= 18;
        } catch (Exception e) {
            // Log error if needed
            // logger.error("Error validating age restriction: " + e.getMessage(), e);
            return false;
        }
    }
}
