package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class DitaOtExtension {
    Closure input
    String output = project.buildDir
    String temp = getDefaultTempDir().getPath()

    Closure properties

    String home = "${System.env.DITA_HOME}"
    String transtype

    final Project project

    static File getDefaultTempDir() {
        String tmpdir = System.getProperty("java.io.tmpdir")

        return new File("${tmpdir}/dita-ot",
                        System.currentTimeMillis().toString())
    }

    DitaOtExtension(Project project) {
        this.project = project
    }
}
