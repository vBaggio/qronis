package com.qronis.service;

import com.qronis.entity.Project;
import com.qronis.entity.Tenant;
import com.qronis.entity.User;
import com.qronis.exception.ResourceNotFoundException;
import com.qronis.repository.ProjectRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID tenantId;
    private UUID userId;
    private Project project;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        Tenant tenant = new Tenant("Qronis");
        tenant.setId(tenantId);

        User user = new User();
        user.setId(userId);

        project = new Project("Projeto Alpha", tenant, user);
        project.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("findByTenantId: deve retornar projetos do tenant")
    void findByTenantId_success() {
        when(projectRepository.findByTenantIdWithCreator(tenantId)).thenReturn(List.of(project));

        List<Project> result = projectService.findByTenantId(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Projeto Alpha");
    }

    @Test
    @DisplayName("findByIdAndTenantId: deve encontrar projeto do tenant")
    void findByIdAndTenantId_success() {
        when(projectRepository.findByIdAndTenantIdWithCreator(project.getId(), tenantId))
                .thenReturn(Optional.of(project));

        Project result = projectService.findByIdAndTenantId(project.getId(), tenantId);

        assertThat(result.getName()).isEqualTo("Projeto Alpha");
    }

    @Test
    @DisplayName("findByIdAndTenantId: deve lançar exceção se projeto não existe no tenant")
    void findByIdAndTenantId_notFound() {
        UUID randomId = UUID.randomUUID();
        when(projectRepository.findByIdAndTenantIdWithCreator(randomId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findByIdAndTenantId(randomId, tenantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Projeto não encontrado");
    }

    @Test
    @DisplayName("create: deve criar e salvar projeto")
    void create_success() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.create("Projeto Alpha", tenantId, userId);

        assertThat(result).isNotNull();
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("update: deve atualizar nome do projeto")
    void update_success() {
        when(projectRepository.findByIdAndTenantIdWithCreator(project.getId(), tenantId))
                .thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.update(project.getId(), tenantId, "Novo Nome");

        assertThat(result.getName()).isEqualTo("Novo Nome");
    }

    @Test
    @DisplayName("delete: deve excluir projeto do tenant")
    void delete_success() {
        when(projectRepository.findByIdAndTenantIdWithCreator(project.getId(), tenantId))
                .thenReturn(Optional.of(project));

        projectService.delete(project.getId(), tenantId);

        verify(projectRepository).delete(project);
    }
}
