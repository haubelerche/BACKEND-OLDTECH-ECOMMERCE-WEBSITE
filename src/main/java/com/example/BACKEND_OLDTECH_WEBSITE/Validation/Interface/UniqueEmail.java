package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Email đã tồn tại trong hệ thống";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
