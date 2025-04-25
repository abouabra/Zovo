package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.validators.ValidPassword;

/**
 * Data Transfer Object (DTO) for handling user login information.
 * This class is used to encapsulate the email and password
 * provided during the login process.
 * <p>
 * - The {@code email} field is required and must follow a valid email format.
 *   A validation constraint is applied to ensure that it is not blank and
 *   adheres to the standard email format.
 * <p>
 * - The {@code password} field is required and must comply with the
 *   custom {@code ValidPassword} annotation. This ensures that the password satisfies
 *   predefined security requirements, such as inclusion of uppercase letters,
 *   lowercase letters, and numbers.
 */
@AllArgsConstructor
@Data
public class UserLoginDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @ValidPassword
    private String password;
}