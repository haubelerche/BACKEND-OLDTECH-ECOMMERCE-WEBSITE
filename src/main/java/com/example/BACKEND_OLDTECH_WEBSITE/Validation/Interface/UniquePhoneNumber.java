package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator.UniquePhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniquePhoneNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePhoneNumber {
    String message() default "Số điện thoại đã tồn tại trong hệ thống";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
