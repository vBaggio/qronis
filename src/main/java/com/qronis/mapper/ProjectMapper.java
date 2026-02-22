package com.qronis.mapper;

import com.qronis.dto.ProjectResponseDTO;
import com.qronis.entity.Project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "createdByName", source = "createdBy.name")
    ProjectResponseDTO toResponse(Project project);

    List<ProjectResponseDTO> toResponseList(List<Project> projects);
}
