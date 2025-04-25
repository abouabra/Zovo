package me.abouabra.zovo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing the response for user-related operations.
 * This class is used to encapsulate and transfer user-related data such as user ID, username,
 * and email between different layers of the application, particularly as a return type in APIs.
 * <p>
 * Typical use cases for this class include:
 * - Representing user information in API responses.
 * - Ensuring that only necessary and safe user information is exposed externally.
 */
@AllArgsConstructor
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
}
