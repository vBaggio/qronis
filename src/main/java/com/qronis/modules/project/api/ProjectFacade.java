package com.qronis.modules.project.api;

import org.springframework.modulith.NamedInterface;
import java.util.UUID;

@NamedInterface("api")
public interface ProjectFacade {
    void validateProjectBelongsToTenant(UUID projectId, UUID tenantId);
    String getProjectName(UUID projectId);
}
