package com.qronis.modules.project.api.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String id) {
        super("Projeto não encontrado: " + id);
    }

    public ProjectNotFoundException() {
        super("Projeto não encontrado");
    }
}
