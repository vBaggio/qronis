package com.qronis.modules.project.api;

import java.util.UUID;

public interface ProjectFacade {
    void validateProjectBelongsToTenant(UUID projectId, UUID tenantId);
    String getProjectName(UUID projectId);
}
