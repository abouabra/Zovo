package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for managing two-factor authentication (2FA) token and code information.
 * <p>
 * This class encapsulates the temporary token and the 2FA code used to enhance
 * account security through additional authentication steps.
 * <ul>
 * <li>{@code token}: A non-blank temporary token for 2FA processing.</li>
 * <li>{@code code}: A 6-digit numeric 2FA verification code.</li>
 * </ul>
 * <p>
 * Provides validation annotations to ensure data integrity during authentication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFaTokenAndCodeDTO {
    @NotBlank(message = "Temp token cannot be blank")
    private String token;

    @Pattern(regexp = "^[0-9]{6}$", message = "2FA code must be 6 digits")
    @NotBlank(message = "2FA code cannot be blank")
    @Size(min = 6, max = 6, message = "2FA code must be exactly 6 digits")
    private String code;
}
