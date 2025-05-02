package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.PasswordResetDTO;
import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.io.Serializable;


/**
 * <p>The <code>UserMapper</code> interface defines mapping functionality between
 * <code>User</code> entities and their corresponding DTO classes.</p>
 *
 * <ul>
 *     <li>Converts DTOs to <code>User</code> entities.</li>
 *     <li>Converts <code>User</code> entities to response DTOs.</li>
 *     <li>Utilizes MapStruct for automatic implementation generation.</li>
 *     <li>Depends on <code>RoleMapper</code> for role conversions.</li>
 * </ul>
 *
 * <p>Designed for use in Spring-based applications.</p>
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends Serializable {
    User toUser(UserRegisterDTO userRegisterDTO);

    UserResponseDTO toDTO(User user);
}
