package me.abouabra.zovo.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.abouabra.zovo.dtos.UserRegisterDTO;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRegisterDTO> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserRegisterDTO dto, ConstraintValidatorContext context) {
        if (    dto == null ||
                (dto.getPassword() != null && !dto.getPassword().equals(dto.getPasswordConfirmation()))
        ) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match.")
                    .addPropertyNode("passwordConfirmation")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}