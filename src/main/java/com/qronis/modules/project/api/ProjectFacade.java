package com.qronis.modules.project.api;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ProjectFacade {
    void validateProjectBelongsToTenant(UUID projectId, UUID tenantId);
    Map<UUID, String> getProjectNames(Set<UUID> projectIds);
}
