package com.qronis.mapper;

import com.qronis.dto.ProjectResponse;
import com.qronis.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "createdByName", source = "createdBy.name")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);
}
