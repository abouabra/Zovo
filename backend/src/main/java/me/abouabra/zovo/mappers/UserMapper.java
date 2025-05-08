package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.UserDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    public abstract User toUser(UserRegisterDTO dto);

    @Mapping(target = "avatar", ignore = true)
    public abstract UserDTO toDTOBase(User user);

    public UserDTO toDTO(User user, AvatarStorageService avatarStorageService) {
        if (user == null) {
            return null;
        }

        UserDTO dto = toDTOBase(user);
        setAvatar(dto, user, avatarStorageService);
        return dto;
    }

    protected void setAvatar(UserDTO dto, User user, AvatarStorageService avatarStorageService) {
        if (user.getAvatarKey() != null) {
            dto.setAvatar(avatarStorageService.getAvatarUrl(user.getAvatarKey()));
        }
    }
}