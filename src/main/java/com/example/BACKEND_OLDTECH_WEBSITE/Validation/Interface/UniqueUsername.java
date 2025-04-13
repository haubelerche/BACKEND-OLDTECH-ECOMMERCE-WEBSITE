package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator.UniqueUsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {
    String message() default "Username đã tồn tại trong hệ thống";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
