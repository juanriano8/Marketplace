package com.marketplace.api.modules.user.application.mapper;

import com.marketplace.api.modules.user.application.dto.UserResponse;
import com.marketplace.api.modules.user.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
