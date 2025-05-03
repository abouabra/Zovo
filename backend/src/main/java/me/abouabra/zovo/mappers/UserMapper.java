package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.*;
import me.abouabra.zovo.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.io.Serializable;


/**
 * <p>UserMapper is an interface for mapping between {@code User}, {@code UserDTO},
 * and {@code UserRegisterDTO} objects.</p>
 *
 * <p>It uses MapStruct to automatically generate mapping implementations at compile time.
 * The mappings include converting user data and normalizing email fields.</p>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li>{@code componentModel = "spring"}: Enables integration as a Spring Bean.</li>
 *   <li>{@code unmappedTargetPolicy = ReportingPolicy.IGNORE}: Ignores unmapped targets.</li>
 * </ul>
 *
 * <p>This mapper is {@code Serializable} for safe object serialization.</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends Serializable {
    @Mapping(target = "email", expression = "java(userRegisterDTO.getEmail().toLowerCase(java.util.Locale.ROOT))")
    User toUser(UserRegisterDTO userRegisterDTO);

    UserDTO toDTO(User user);
}
