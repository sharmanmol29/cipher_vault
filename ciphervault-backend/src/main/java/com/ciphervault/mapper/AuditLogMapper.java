package com.ciphervault.mapper;

import com.ciphervault.dto.admin.response.AuditLogRowResponse;
import com.ciphervault.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {

	@Mapping(target = "userId", expression = "java(log.getUser() == null ? null : log.getUser().getId())")
	@Mapping(target = "userEmail", expression = "java(log.getUser() == null ? null : log.getUser().getEmail())")
	AuditLogRowResponse toRow(AuditLog log);
}
