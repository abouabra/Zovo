package me.abouabra.zovo.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;


/**
 * Validates that the "password" and "password confirmation" fields in an annotated class match.
 * <p>
 * This class is used in conjunction with the {@code PasswordMatches} annotation. It ensures
 * that the values of the "password" and "password confirmation" fields in a target object
 * are identical.
 * </p>
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            Method getPassword = obj.getClass().getMethod("getPassword");
            Method getPasswordConfirmation = obj.getClass().getMethod("getPasswordConfirmation");

            String password = (String) getPassword.invoke(obj);
            String passwordConfirmation = (String) getPasswordConfirmation.invoke(obj);

            if (password == null || !password.equals(passwordConfirmation)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Passwords do not match.")
                        .addPropertyNode("passwordConfirmation")
                        .addConstraintViolation();
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}