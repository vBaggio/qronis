package com.qronis;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class QronisArchitectureTest {

    ApplicationModules modules = ApplicationModules.of(QronisApplication.class);

    @Test
    void verifyModulithArchitecture() {
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules)
            .writeModuleCanvases()
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
