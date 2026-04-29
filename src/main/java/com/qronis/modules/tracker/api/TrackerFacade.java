package com.qronis.modules.tracker.api;

import java.util.UUID;

public interface TrackerFacade {
    Long getTotalTimeSecondsByProject(UUID projectId, UUID userId);
}
