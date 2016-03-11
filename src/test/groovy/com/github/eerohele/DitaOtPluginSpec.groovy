package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DitaOtPluginSpec extends Specification {
    private static final String DITA = 'dita'

    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    @SuppressWarnings('MethodName')
    def "Apply plugin"() {
        expect:
            project.tasks.findByName(DITA) == null

        when:
            project.apply plugin: DitaOtPlugin

        then:
            Task task = project.tasks.findByName(DITA)
            task != null
            task.group == 'Documentation'

            project.tasks.findByName('clean') != null
    }

    @SuppressWarnings('MethodName')
    def "Load extensions"() {
        given:
            project.apply plugin: DitaOtPlugin

        when:
            project.ditaOt.dir '/opt/dita-ot'

        then:
            project.ditaOt.home.class == File
    }
}
