package com.qronis.modules.tracker.api;

import org.springframework.modulith.NamedInterface;
import java.util.UUID;

@NamedInterface("api")
public interface TrackerFacade {
    Long getTotalTimeSecondsByProject(UUID projectId, UUID userId);
}
