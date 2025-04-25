package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.validators.PasswordMatches;
import me.abouabra.zovo.validators.ValidPassword;

/**
 * Data Transfer Object (DTO) for handling user registration information.
 * This class is used to encapsulate the necessary data for registering a new user,
 * including username, email, password, and password confirmation. Validation
 * constraints are applied to ensure data integrity and adherence to specified rules.
 * <p>
 * The class includes the following validations:
 * <ul>
 * <li>The {@code username} field:
 *     <ul>
 *     <li>Must not be blank</li>
 *     <li>Must be between 3 and 30 characters long</li>
 *     <li>Can only contain alphanumeric characters and underscores</li>
 *     </ul>
 * </li>
 * <li>The {@code email} field:
 *     <ul>
 *     <li>Must not be blank</li>
 *     <li>Must follow a valid email format</li>
 *     </ul>
 * </li>
 * <li>The {@code password} field:
 *     <ul>
 *     <li>Must not be blank</li>
 *     <li>Must comply with the custom {@code ValidPassword} annotation to
 *         ensure predefined security requirements such as containing uppercase
 *         letters, lowercase letters, and numbers</li>
 *     </ul>
 * </li>
 * <li>The {@code passwordConfirmation} field:
 *     <ul>
 *     <li>Must not be blank</li>
 *     <li>Must comply with the custom {@code ValidPassword} annotation</li>
 *     </ul>
 * </li>
 * </ul>
 * <p>
 * In addition, the class is validated at the class-level through the
 * {@code PasswordMatches} annotation to ensure the password and
 * password confirmation fields match.
 */
@AllArgsConstructor
@Data
@PasswordMatches(message = "Passwords do not match")
public class UserRegisterDTO {

    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @ValidPassword
    private String password;

    @NotBlank(message = "Password Confirmation cannot be blank")
    @ValidPassword
    private String passwordConfirmation;
}