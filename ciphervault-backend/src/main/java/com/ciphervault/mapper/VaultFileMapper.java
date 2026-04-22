package com.ciphervault.mapper;

import com.ciphervault.dto.file.response.FileItemResponse;
import com.ciphervault.entity.VaultFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VaultFileMapper {

	@Mapping(target = "folderId", source = "folder.id")
	FileItemResponse toItem(VaultFile file);
}
