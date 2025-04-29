package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.PasswordResetDTO;
import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



/**
 * <p>
 * UserMapper is an interface for mapping between different user-related
 * entities and DTOs. It leverages MapStruct to generate implementation
 * at compile time and uses the Spring component model.
 * </p>
 *
 * <p>
 * Primary functionalities include:
 * </p>
 * <ul>
 *   <li>Mapping User DTOs (e.g., UserRegisterDTO, UserLoginDTO) to User entity.</li>
 *   <li>Mapping User entity to UserResponseDTO.</li>
 * </ul>
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterDTO userRegisterDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(PasswordResetDTO passwordResetDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserLoginDTO userLoginDTO);

    UserResponseDTO toDTO(User user);
}
