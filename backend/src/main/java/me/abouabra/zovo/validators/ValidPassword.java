package me.abouabra.zovo.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must contain at least 1 uppercase, 1 lowercase, and 1 number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
