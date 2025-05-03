package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.abouabra.zovo.annotations.ValidPassword;

import java.io.Serializable;
import java.util.Locale;


/**
 * Data Transfer Object (DTO) representing user login details.
 * <p>
 * This class is used to encapsulate the email and password provided
 * by the user during the login process. It includes validation annotations
 * to ensure proper data format:
 * <ul>
 * <li><b>email</b>: Must be in a valid email format and cannot be blank.</li>
 * <li><b>password</b>: Must adhere to strong password constraints and cannot be blank.</li>
 * </ul>
 * <p>
 * Utilizes Lombok annotations to reduce boilerplate code.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLoginDTO implements Serializable {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @ValidPassword
    private String password;

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}