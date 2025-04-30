package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for handling Two-Factor Authentication (2FA) codes.
 * <p>
 * This class encapsulates the single-use or recovery code needed for verifying
 * user identity in 2FA processes. It includes validation annotations to ensure
 * the code is properly formatted and not blank.
 * <ul>
 * <li>{@code code}: A required field that must either be a 6-digit code
 *     or an 8-character recovery code.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFaCodeDTO {
    @Pattern(
            regexp = "^([0-9]{6}|[A-Z0-9_-]{8})$",
            message = "Must be a 6-digit code or an 8-character recovery code"
    )
    @NotBlank(message = "Code cannot be blank")
    private String code;
}

