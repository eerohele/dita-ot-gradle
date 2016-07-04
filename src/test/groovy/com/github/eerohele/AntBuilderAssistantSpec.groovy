package com.github.eerohele

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import org.gradle.api.internal.project.antbuilder.DefaultIsolatedAntBuilder

class AntBuilderAssistantSpec extends Specification {
    private static final String DITA = 'dita'
    private static final String DITA_OT = 'ditaOt'

    Project project
    String ditaHome

    def setup() {
        ditaHome = System.getProperty('dita.home')
        project = ProjectBuilder.builder().build()
        project.configurations.create(DITA)

        project.tasks.create(name: DITA_OT, type: DitaOtSetupTask) {
            dir ditaHome
        }

        project.ditaOt.dir ditaHome
    }

    @SuppressWarnings('MethodName')
    def "Getting Ant builder for the first time sets thread local Ant builder and returns it"() {
        expect:
            AntBuilderAssistant.THREAD_LOCAL_ANT_BUILDER.get() == null

        when:
            DefaultIsolatedAntBuilder antBuilder = AntBuilderAssistant.getAntBuilder(project)

        then:
            antBuilder.getClass() == DefaultIsolatedAntBuilder

        and:
            AntBuilderAssistant.THREAD_LOCAL_ANT_BUILDER.get().getClass() == DefaultIsolatedAntBuilder

        cleanup:
            AntBuilderAssistant.THREAD_LOCAL_ANT_BUILDER.remove()
    }

    @SuppressWarnings('MethodName')
    def "Getting Ant builder for a second time returns the thread local instance"() {
        expect:
            AntBuilderAssistant.THREAD_LOCAL_ANT_BUILDER.get() == null

        when:
            DefaultIsolatedAntBuilder first = AntBuilderAssistant.getAntBuilder(project)
            DefaultIsolatedAntBuilder second = AntBuilderAssistant.getAntBuilder(project)

        then:
            first.is(second)

        cleanup:
            AntBuilderAssistant.THREAD_LOCAL_ANT_BUILDER.remove()
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def "Making Ant builder creates an Ant builder with DITA-OT classpath"() {
        when:
            DefaultIsolatedAntBuilder antBuilder = AntBuilderAssistant.makeAntBuilder(project)

            Class<?> ditaOtMainClass = null

            antBuilder.execute {
                ditaOtMainClass = antProject.getClass().getClassLoader().findClass('org.dita.dost.invoker.Main')
            }

        then:
            ditaOtMainClass.getName() == 'org.dita.dost.invoker.Main'
    }
}
