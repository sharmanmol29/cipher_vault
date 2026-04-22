package com.ciphervault.mapper;

import com.ciphervault.dto.admin.response.UserAdminRowResponse;
import com.ciphervault.dto.auth.response.UserMeResponse;
import com.ciphervault.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

	UserMeResponse toMe(User user);

	UserAdminRowResponse toAdminRow(User user);
}
