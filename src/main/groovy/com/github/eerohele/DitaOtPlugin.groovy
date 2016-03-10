package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.file.FileCollection

class DitaOtPlugin implements Plugin<Project> {
    static final String DITA = 'dita'
    static final String DITA_OT = 'ditaOt'
    static final ConfigObject MESSAGES = new ConfigSlurper().parse(Messages).messages

    Double getCurrentJavaVersion() {
        System.getProperty('java.specification.version').toDouble()
    }

    FileCollection getClasspath(Project project) {
        project.fileTree(dir: project.ditaOt.home).matching {
            include(
                'resources/',
                'lib/**/*.jar',
                'plugins/org.dita.pdf2/lib/fo.jar',
                'plugins/org.dita.pdf2/build/libs/fo.jar'
            )

            exclude(
                'lib/ant-launcher.jar',
                'lib/ant.jar'
            )
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

        if (getCurrentJavaVersion() < 1.8) {
            project.logger.warn(MESSAGES.javaVersionWarning)
        }

        project.afterEvaluate {
            task.conventionMapping.with {
                ditaOtClasspath = { getClasspath(project) }
            }
        }
    }

}
