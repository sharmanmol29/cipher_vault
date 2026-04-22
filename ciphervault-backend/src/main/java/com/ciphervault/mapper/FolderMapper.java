package com.ciphervault.mapper;

import com.ciphervault.dto.folder.response.FolderItemResponse;
import com.ciphervault.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FolderMapper {

	@Mapping(target = "parentId", source = "parent.id")
	FolderItemResponse toItem(Folder folder);
}
