package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserRequestDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequestDTO userRequestDTO);
    User toUser(UserRegisterDTO userRegisterDTO);
    UserResponseDTO toDTO(User user);
}
