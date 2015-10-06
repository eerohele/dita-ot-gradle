package com.github.eerohele

import org.gradle.api.Project

class DitaOtExtension {
    final Project project

    String dir = "${System.env.DITA_HOME}"

    DitaOtExtension(Project project) {
        this.project = project
    }
}
