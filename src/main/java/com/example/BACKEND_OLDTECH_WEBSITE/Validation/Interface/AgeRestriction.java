package com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Validator.AgeRestrictionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeRestrictionValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeRestriction {
    String message() default  "Bạn phải trên 18 tuổi để đăng ký tài khoản";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
