package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator.UniquePhoneNumberValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation to validate that a phone number is unique in the system.
 */
@Documented
@Constraint(validatedBy = UniquePhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePhoneNumber {
    String message() default "Phone number already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
