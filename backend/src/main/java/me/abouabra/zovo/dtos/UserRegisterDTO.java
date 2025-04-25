package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.validators.PasswordMatches;
import me.abouabra.zovo.validators.ValidPassword;

@AllArgsConstructor
@Data
@PasswordMatches(message = "Passwords do not match")
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
}