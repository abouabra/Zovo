package me.abouabra.zovo.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * A custom validation annotation used to enforce strong password policies.
 *
 * <p>
 * This annotation is designed to be applied to fields in data transfer objects (DTOs) or entities
 * where a secure password must adhere to specific constraints. The validation logic ensures that
 * passwords meet the following requirements:
 * </p>
 *
 * <ul>
 * <li>At least one uppercase letter.</li>
 * <li>At least one lowercase letter.</li>
 * <li>At least one numeric digit.</li>
 * </ul>
 *
 * <p><strong>Annotation Attributes:</strong></p>
 * <ul>
 * <li><b>message</b>: Customizable error message returned when the validation fails.
 * Defaults to "Password must contain at least 1 uppercase, 1 lowercase, and 1 number".</li>
 * <li><b>groups</b>: Allows grouping of constraints for selective validation. Defaults to an empty array.</li>
 * <li><b>payload</b>: Custom metadata that can be used by clients of the Jakarta Bean Validation
 * API to associate additional information with a constraint. Defaults to an empty array.</li>
 * </ul>
 *
 * <p><strong>Supported Element Types:</strong></p>
 * <ul>
 * <li>Fields</li>
 * </ul>
 *
 * <p><strong>Retention Policy:</strong></p>
 * <ul>
 * <li>Runtime</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * This annotation works in conjunction with the {@code PasswordValidator} class, which contains
 * the actual validation logic. When applied to a field, the annotation ensures that the provided value
 * complies with the defined password rules. If the validation fails, the specified error message
 * will be returned.
 *
 * <p>The annotation leverages the Jakarta Bean Validation framework for validation operations.</p>
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must contain at least 1 uppercase, 1 lowercase, and 1 number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
