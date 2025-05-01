package me.abouabra.zovo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO implements Serializable {
    private Long id;

    @NotBlank(message = "Role name cannot be blank")
    private String name;
}
