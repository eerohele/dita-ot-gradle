package com.github.eerohele

import org.gradle.api.Project

class DitaOtExtension {
    final Project project
    File home

    void dir(Object d) {
        home = project.file(d)
    }

    DitaOtExtension(Project project) {
        this.project = project
    }
}
