package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.file.FileCollection

class DitaOtPlugin implements Plugin<Project> {
    static final String DITA = 'dita'
    static final String DITA_OT = 'ditaOt'

    FileCollection getClasspath(Project project) {
        project.fileTree(dir: project.ditaOt.home).matching {
            include('lib/**/*.jar', 'plugins/org.dita.pdf2/lib/fo.jar')
        }
    }

    @Override
    void apply(Project project) {
        project.apply plugin: 'base'

        project.extensions.create(DITA_OT, DitaOtExtension, project)

        def task = project.task(
            DITA,
            type: DitaOtTask,
            group: 'Documentation',
            description: 'Publishes DITA documentation with DITA Open Toolkit.'
        )

        project.afterEvaluate {
            task.conventionMapping.with {
                ditaOtClasspath = { getClasspath(project) }
            }
        }
    }

}
