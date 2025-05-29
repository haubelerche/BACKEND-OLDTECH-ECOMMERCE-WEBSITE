package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator.AgeRestrictionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a user's age is at least 18 years old.
 */
@Documented
@Constraint(validatedBy = AgeRestrictionValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeRestriction {
    String message() default "Người dùng phải đủ 18 tuổi trở lên";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
