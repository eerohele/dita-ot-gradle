package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Plugin

class DitaOtPlugin implements Plugin<Project> {
    static final String DITA_OT = 'ditaOt'

    /** Add runtime and provided dependencies to Ant classloader.
     *
     * @since 0.1.0
     */
    void augmentAntClassLoader(Project project) {
        def classLoader = org.apache.tools.ant.Project.class.classLoader
        def runtime = project.configurations.runtime as Set
        def provided = project.configurations.provided as Set

        runtime.plus(provided).each {
            classLoader.addURL(project.file(it).toURI().toURL())
        }
    }

    void setRepositories(Project project) {
        project.repositories {
          mavenCentral()
        }
    }

    void setConfigurations(Project project) {
        project.configurations {
            runtime
            provided
        }
    }

    void setDependencies(Project project) {
        String ditaHome = project.ditaOt.home

        project.dependencies {
            runtime 'commons-io:commons-io:2.4'
            runtime 'commons-codec:commons-codec:1.9'
            runtime 'xerces:xercesImpl:2.11.0'
            runtime 'xml-apis:xml-apis:1.4.01'
            runtime 'xml-resolver:xml-resolver:1.2'
            runtime 'net.sourceforge.saxon:saxon:9.1.0.8:dom'
            runtime 'net.sourceforge.saxon:saxon:9.1.0.8'
            runtime 'com.ibm.icu:icu4j:54.1'
            runtime 'org.apache.ant:ant:1.9.4'
            runtime 'org.apache.ant:ant-launcher:1.9.4'
            runtime 'org.apache.ant:ant-apache-resolver:1.9.4'

            provided project.files("${ditaHome}/lib/dost.jar")
            provided project.files("${ditaHome}/plugins/org.dita.pdf2/lib/fo.jar")
            provided project.files("${ditaHome}/lib")
            provided project.files("${ditaHome}/resources")
        }
    }

    @Override
    void apply(Project project) {
        project.apply plugin: 'base'

        project.extensions.create(DITA_OT, DitaOtExtension, project)

        // Project extensions aren't available before afterEvaluate.
        project.afterEvaluate {
            // FIXME: These shouldn't simply override the properties defined by
            // the user in the buildfile but rather merge with them. I think?
            setRepositories(project)
            setConfigurations(project)
            setDependencies(project)
            augmentAntClassLoader(project)
        }

        project.task(DITA_OT, type: DitaOtTask, group: 'Documentation') {
            // Define the parent directory of each input file as the inputs for
            // the DITA-OT task. This causes the task to be considered up to
            // date if none of the files in the parent folder have changed.
            //
            // Conversely, if any file in the source folder changes, the task
            // will no longer be considered up to date.
            //
            // TODO: It might be worth considering whether it would make more
            // sense to parse the input DITA map, get all files associated with
            // the DITA map, and use that list instead. That's going to be
            // slower, though.
            inputs.files {
                project.files(project.ditaOt.input).collect {
                    it.getParent()
                }
            }

            outputs.dir { project.ditaOt.output }
        }
    }

}
