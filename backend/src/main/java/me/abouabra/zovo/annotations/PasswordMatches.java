package me.abouabra.zovo.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation used to verify that two password fields in a class match.
 * <p>
 * This annotation is typically applied at the class level and works in conjunction with
 * a corresponding validator, {@code PasswordMatchesValidator}, which performs the validation
 * logic. The primary use case for this annotation is to ensure that a "password" and
 * "password confirmation" field in a data transfer object (DTO), such as {@code UserRegisterDTO},
 * contain identical values.
 * </p>
 * <p>
 * The annotation can be customized with the following attributes:
 * <ul>
 * <li><b>message</b>: The error message to be returned if the validation fails. Defaults to "Passwords do not match".</li>
 * <li><b>groups</b>: Provides the ability to specify validation groups to which this constraint belongs.</li>
 * <li><b>payload</b>: Custom payload objects that can be used to carry metadata information with the constraint.</li>
 * </ul>
 * <p>
 * Example: To use this annotation, add it to a class where password matching validation
 * is required, e.g., a user registration DTO. The annotated class is then validated to ensure
 * that the "password" field matches the "password confirmation" field.
 * </p>
 *
 * <p><strong>Supported Element Types:</strong></p>
 * <ul>
 * <li>Classes</li>
 * </ul>
 * <p><strong>Retention Policy:</strong></p>
 * <ul>
 * <li>Runtime</li>
 * </ul>
 *
 * <p>The annotation leverages Jakarta Bean Validation framework for validation operations.</p>
 */
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
