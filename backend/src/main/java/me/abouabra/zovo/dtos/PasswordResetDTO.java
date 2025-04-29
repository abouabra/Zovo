package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.validators.PasswordMatches;
import me.abouabra.zovo.validators.ValidPassword;


/**
 * Data Transfer Object (DTO) for handling password reset requests.
 * <p>
 * This class is used to encapsulate the necessary information required
 * to reset a user's password, including:
 * <ul>
 * <li>{@code token}: A reset token to validate the request.</li>
 * <li>{@code password}: The new password.</li>
 * <li>{@code passwordConfirmation}: Confirmation of the new password.</li>
 * </ul>
 * <p>
 * Validation annotations are applied to ensure field constraints, such as
 * non-empty values and password strength.
 */
@AllArgsConstructor
@Data
@PasswordMatches
public class PasswordResetDTO {
    @NotBlank(message = "Token cannot be blank")
    private String token;

    @NotBlank(message = "Password cannot be blank")
    @ValidPassword
    private String password;

    @NotBlank(message = "Password Confirmation cannot be blank")
    @ValidPassword
    private String passwordConfirmation;
}