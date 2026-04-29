package com.qronis.modules.project.application;

import com.qronis.modules.project.web.dto.ProjectResponseDTO;
import com.qronis.modules.project.domain.entity.Project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponseDTO toResponse(Project project);

    List<ProjectResponseDTO> toResponseList(List<Project> projects);
}
