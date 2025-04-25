package me.abouabra.zovo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private ZonedDateTime createdAt;
}
