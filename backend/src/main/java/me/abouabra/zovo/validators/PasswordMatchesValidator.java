package me.abouabra.zovo.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.abouabra.zovo.dtos.UserRegisterDTO;

/**
 * A custom validator that ensures the password and password confirmation fields in the
 * {@code UserRegisterDTO} object are identical. This validator is used in conjunction with
 * the {@link PasswordMatches} annotation.
 *
 * <p>
 * The {@code PasswordMatchesValidator} class implements the {@code ConstraintValidator}
 * interface, defining the custom validation logic for the {@code PasswordMatches} annotation.
 * It validates whether the provided {@code password} and {@code passwordConfirmation} fields
 * are equal within an instance of {@code UserRegisterDTO}.
 * </p>
 *
 * <p>
 * <strong>Validation Logic:</strong>
 * <ul>
 * <li>Returns {@code false} if the {@code dto} object is {@code null}.</li>
 * <li>Returns {@code false} and adds a custom constraint violation message
 * ("Passwords do not match") if the {@code password} and {@code passwordConfirmation} fields
 * do not match.</li>
 * <li>Returns {@code true} if the fields are equal.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 * <li>Initializes the validator (although no specific initialization is implemented in this
 * case).</li>
 * <li>Implements the {@code isValid} method to define the validation logic for
 * {@code PasswordMatches}.</li>
 * <li>Builds and adds a custom constraint violation message to the validation context if the
 * validation fails.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Usage:</strong>
 * The {@link PasswordMatches} annotation should be applied at the class level on DTO objects
 * such as {@link UserRegisterDTO}. This validator automatically processes the annotation
 * during the validation process.
 * </p>
 *
 * <p><strong>Dependencies:</strong></p>
 * <ul>
 * <li>Jakarta Bean Validation API ({@code ConstraintValidator} and
 * {@code ConstraintValidatorContext})</li>
 * <li>{@link PasswordMatches} annotation</li>
 * <li>{@link UserRegisterDTO} class for retrieving and comparing the {@code password} and
 * {@code passwordConfirmation} fields</li>
 * </ul>
 *
 * <p>
 * <strong>Important Notes:</strong>
 * <ul>
 * <li>This validator assumes that the password fields have already passed
 * {@code @NotBlank} and other validations such as {@link ValidPassword} before being compared.</li>
 * <li>The custom message for the validation failure can be customized via the annotation's
 * {@code message} attribute when the {@code PasswordMatches} annotation is applied.</li>
 * </ul>
 */
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