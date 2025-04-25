package me.abouabra.zovo.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * A Validator implementation to ensure that a given password string complies with specific
 * security constraints defined by the {@link ValidPassword} annotation.
 *
 * <p>
 * The {@code PasswordValidator} class is a custom implementation of {@link ConstraintValidator}
 * for the {@link ValidPassword} annotation. It enforces strong password rules to enhance
 * security, ensuring that the provided password meets the following criteria:
 * </p>
 *
 * <ul>
 * <li>Contains at least one uppercase letter.</li>
 * <li>Contains at least one lowercase letter.</li>
 * <li>Contains at least one numeric digit.</li>
 * <li>Is at least 8 characters long.</li>
 * </ul>
 *
 * <p>
 * The validation logic is based on a compiled {@link Pattern} using a regular expression
 * that verifies these rules.
 * </p>
 *
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>Processes the {@code ValidPassword} annotation applied to fields that represent passwords
 * in data models.</li>
 * <li>Implements {@code isValid} method to perform the actual validation logic on the password
 * value provided.</li>
 * <li>Returns {@code true} if the password satisfies the defined criteria, otherwise returns
 * {@code false}.</li>
 * <li>Handles {@code null} values gracefully by returning {@code false}, ensuring null passwords
 * are rejected.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Usage:</strong>
 * This validator is triggered automatically during the Jakarta Bean Validation process when
 * the {@code @ValidPassword} annotation is applied to a field. For example, the annotation
 * can be applied to a password field in a DTO object, and this validator will validate it
 * accordingly during runtime.
 * </p>
 *
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link ValidPassword}: The annotation used to mark fields requiring password validation.</li>
 * <li>{@link ConstraintValidator}: The base interface that provides the validation infrastructure.</li>
 * <li>{@link Pattern}: Utility class for compiling and using regular expressions.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Validation Regular Expression:</strong>
 * The password validation pattern is defined as:
 * <pre>"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"</pre>
 * This ensures:
 * <ul>
 * <li>At least one lowercase letter.</li>
 * <li>At least one uppercase letter.</li>
 * <li>At least one numeric digit.</li>
 * <li>Minimum of eight characters in total.</li>
 * </ul>
 * </p>
 *
 * <p>This class is designed to be stateless and reusable within a validation context.</p>
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
