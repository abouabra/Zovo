package me.abouabra.zovo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.abouabra.zovo.services.storage.AvatarStorageService;

import java.io.Serializable;
import java.util.Locale;


/**
 * Data Transfer Object (DTO) representing a simplified user model.
 * <p>
 * This class encapsulates basic user details such as id, username, and email.
 * It provides methods for accessing and mutating the user properties, with
 * email normalization functionality.
 * <p>
 * Uses Lombok annotations to generate boilerplate code, and implements
 * {@code Serializable} for easy object serialization.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String avatar;

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
