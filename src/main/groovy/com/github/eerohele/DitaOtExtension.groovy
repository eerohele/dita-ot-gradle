package com.github.eerohele

import org.gradle.api.Project

class DitaOtExtension {
    final Project project

    String home = "${System.env.DITA_HOME}"

    DitaOtExtension(Project project) {
        this.project = project
    }
}
