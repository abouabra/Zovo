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
 * The {@code UserMapper} interface is a MapStruct mapper that provides
 * functionality for mapping between {@code User} entities and Data Transfer Objects (DTOs).
 * It supports both entity-to-DTO and DTO-to-entity conversions, automating object transformation
 * while adhering to specified mapping rules.
 * </p>
 *
 * <p>
 * This mapper uses the {@code @Mapper} annotation with a {@code spring} component model,
 * allowing it to be registered as a Spring Bean. It also makes use of the {@code RoleMapper}
 * to handle mappings of associated {@code Role} objects where necessary.
 * </p>
 *
 * <p>
 * The mapping operations in this interface include:
 * </p>
 * <ul>
 *   <li>
 *     Converting a {@link UserRequestDTO} to a {@link User} entity.
 *     <ul>
 *       <li>Ignores the {@code id}, {@code createdAt}, and {@code roles} fields of the {@code User} entity.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     Converting a {@link UserRegisterDTO} to a {@link User} entity.
 *     <ul>
 *       <li>Ignores the {@code id}, {@code createdAt}, and {@code roles} fields of the {@code User} entity.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     Converting a {@link UserLoginDTO} to a {@link User} entity.
 *     <ul>
 *       <li>Ignores the {@code id}, {@code createdAt}, and {@code roles} fields of the {@code User} entity.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     Converting a {@link User} entity to a {@link UserResponseDTO}.
 *     <ul>
 *       <li>Maps the {@code id}, {@code username}, and {@code email} fields from the {@code User} entity.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * The {@code UserMapper} ensures type safety and minimizes boilerplate code
 * for object transformation, making it easier to maintain mapping logic
 * across the application.
 * </p>
 *
 * <p><strong>Additional Notes:</strong></p>
 * <ul>
 *   <li>The {@code id}, {@code createdAt}, and {@code roles} of the {@link User} entity
 *       are explicitly ignored in the DTO-to-entity mappings.</li>
 *   <li>MapStruct generates the implementation of this mapper during compilation,
 *       enabling efficient, compile-time verified mappings.</li>
 * </ul>
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRequestDTO userRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterDTO userRegisterDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserLoginDTO userLoginDTO);

    UserResponseDTO toDTO(User user);
}
