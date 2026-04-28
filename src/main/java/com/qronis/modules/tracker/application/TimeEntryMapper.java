package com.qronis.modules.tracker.application;

import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;

import com.qronis.modules.project.api.ProjectFacade;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TimeEntryMapper {

    protected ProjectFacade projectFacade;

    @Autowired
    public void setProjectFacade(ProjectFacade projectFacade) {
        this.projectFacade = projectFacade;
    }

    @Mapping(target = "projectName", expression = "java(projectFacade.getProjectName(timeEntry.getProjectId()))")
    public abstract TimeEntryResponseDTO toResponse(TimeEntry timeEntry);

    public abstract List<TimeEntryResponseDTO> toResponseList(List<TimeEntry> timeEntries);
}
