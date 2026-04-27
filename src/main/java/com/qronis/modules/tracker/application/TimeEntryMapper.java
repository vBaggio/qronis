package com.qronis.modules.tracker.application;

import com.qronis.modules.tracker.api.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeEntryMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    TimeEntryResponseDTO toResponse(TimeEntry timeEntry);

    List<TimeEntryResponseDTO> toResponseList(List<TimeEntry> timeEntries);
}
