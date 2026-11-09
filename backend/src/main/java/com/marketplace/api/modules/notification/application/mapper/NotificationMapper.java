package com.marketplace.api.modules.notification.application.mapper;

import com.marketplace.api.modules.notification.application.dto.NotificationResponse;
import com.marketplace.api.modules.notification.domain.model.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
