package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.RoleDTO;
import me.abouabra.zovo.models.Role;
import org.mapstruct.Mapper;

/**
 * <p>
 * RoleMapper is an interface that provides functionality to map between
 * {@code Role} entities and {@code RoleDTO} data transfer objects. It is
 * annotated with {@code @Mapper} from MapStruct, which generates the
 * implementation for this interface at compile time.
 * </p>
 *
 * <p>
 * The {@code RoleMapper} facilitates the conversion of data between the
 * persistence layer (using the {@code Role} entity) and the service or
 * presentation layer (using the {@code RoleDTO}) in a type-safe and efficient manner.
 * </p>
 *
 * <p>
 * Primary operations include:
 * </p>
 * <ul>
 *   <li>Mapping a {@link RoleDTO} to a {@link Role} entity.</li>
 *   <li>Mapping a {@link Role} entity to a {@link RoleDTO}.</li>
 * </ul>
 *
 * <p>
 * The implementation will use the {@code spring} component model, allowing
 * it to be automatically detected and loaded as a Spring Bean.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toUser(RoleDTO dto);
    RoleDTO toDTO(Role role);
}