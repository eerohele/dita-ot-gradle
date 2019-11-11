package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Plugin

class DitaOtPlugin implements Plugin<Project> {
    static final String DITA = 'dita'
    static final String DITA_OT = 'ditaOt'
    static final ConfigObject MESSAGES = new ConfigSlurper().parse(Messages).messages

    @Override
    void apply(Project project) {
        project.apply plugin: 'base'

        project.task(
            DITA_OT,
            type: DitaOtSetupTask,
            group: 'Documentation',
            description: 'Set up DITA Open Toolkit'
        )

        project.task(
            DITA,
            type: DitaOtTask,
            group: 'Documentation',
            description: 'Publishes DITA documentation with DITA Open Toolkit.'
        )
    }
}
