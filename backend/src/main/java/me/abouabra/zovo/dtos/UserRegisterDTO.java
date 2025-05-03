package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.annotations.PasswordMatches;
import me.abouabra.zovo.annotations.ValidPassword;

import java.util.Locale;


/**
 * Data Transfer Object (DTO) for registering a new user.
 * <p>
 * This class encapsulates the user registration data, including
 * username, email, password, and password confirmation. It also
 * includes validation to ensure data integrity:
 * <ul>
 * <li>{@code username}: Alphanumeric with underscores, 3-30 characters.</li>
 * <li>{@code email}: Valid email format ensured.</li>
 * <li>{@code password} & {@code passwordConfirmation}: Match enforced and must meet strong password requirements.</li>
 * </ul>
 * <p>
 * Includes a custom annotation {@code @PasswordMatches} to validate password confirmation.
 */
@AllArgsConstructor
@Data
@PasswordMatches
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

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}