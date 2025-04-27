package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserRequestDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * <p>
 * UserMapper is an interface utilized to map between User entities and
 * related Data Transfer Objects (DTOs) such as UserRequestDTO, UserRegisterDTO,
 * and UserResponseDTO.
 * </p>
 *
 * <p>
 * It leverages MapStruct for automatic mapping and uses the RoleMapper
 * for role-related transformations. The implementation will adhere to the
 * Spring component model.
 * </p>
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterDTO userRegisterDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserLoginDTO userLoginDTO);

    UserResponseDTO toDTO(User user);
}
