package me.abouabra.zovo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for enabling Two-Factor Authentication (2FA) challenge.
 * <p>
 * This class represents the data required to start a 2FA challenge process.
 * It includes the following fields:
 * <ul>
 * <li>{@code token}: A unique temporary token for the 2FA challenge.</li>
 * <li>{@code provider}: The provider managing the 2FA challenge (e.g., SMS, Email, etc.).</li>
 * </ul>
 * <p>
 * Utilizes Lombok annotations to reduce boilerplate code.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFaChallengeEnabledDTO {
    private String token;
    private String provider;
}
