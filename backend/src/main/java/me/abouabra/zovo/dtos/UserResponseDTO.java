package me.abouabra.zovo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;



/**
 * Data Transfer Object (DTO) representing a user's response data.
 * <p>
 * This class encapsulates the user information to be sent as part
 * of API responses. It includes:
 * <ul>
 * <li>{@code id}: The unique identifier of the user.</li>
 * <li>{@code username}: The username of the user.</li>
 * <li>{@code email}: The email address of the user.</li>
 * <li>{@code roles}: A set of roles associated with the user.</li>
 * </ul>
 * <p>
 * Utilizes Lombok annotations for boilerplate code reduction.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponseDTO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private Set<RoleDTO> roles;
}
