package com.qronis.modules.tracker.application;

import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TimeEntryMapper {

    @Mapping(target = "projectName",
             expression = "java(projectNames.getOrDefault(timeEntry.getProjectId(), null))")
    TimeEntryResponseDTO toResponse(TimeEntry timeEntry, @Context Map<UUID, String> projectNames);
}
