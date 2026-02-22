package com.qronis.mapper;

import com.qronis.dto.TimeEntryResponseDTO;
import com.qronis.entity.TimeEntry;

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
