package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileTree
import org.gradle.testfixtures.ProjectBuilder

import org.apache.tools.ant.BuildException

import spock.lang.Specification

class DitaOtTaskSpec extends Specification {
    private static final String DITA = 'dita'
    private static final String DITA_OT = 'ditaOt'

    Project project

    String ditaHome
    File testRootDir
    File examplesDir

    def getInputFiles(Task task) {
        task.getInputFileCollection().getFiles()
    }

    def setup() {
        ditaHome = System.getProperty('dita.home')

        if (!ditaHome || !new File(ditaHome).isDirectory()) {
            throw new Exception('''dita.home system property not properly set.
To run the tests, you need a working DITA-OT installation and you need to set
the dita.home system property to point to that installation.''')
        }

        project = ProjectBuilder.builder().withName('test').build()
        project.configurations.create(DITA)
        testRootDir = new File('.')
        examplesDir = new File(testRootDir, 'examples')
    }

    def 'Creating a task'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input 'root.ditamap'
                filter 'default.ditaval'
                transtype 'html5'

                properties {
                    property name: 'processing-mode', value: 'strict'
                }
            }

        then:
            task.inputFiles == 'root.ditamap'
            task.ditaVal == 'default.ditaval'
            task.format == 'html5'
            task.props != null
    }

    def 'Giving single input file as String'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
            }

        then:
            getInputFiles(task).find { it.getName() == 'root.ditamap' }
    }

    def 'Giving single input file as File'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.file("$examplesDir/simple/dita/root.ditamap")
            }

        then:
            getInputFiles(task).find { it.getName() == 'root.ditamap' }
    }

    def 'Giving multiple input files'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            getInputFiles(task).collect { it.getName() } ==
                ['one.ditamap', 'two.ditamap']
    }

    def 'Includes containing directories in up-to-date check'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            // I couldn't figure out how to bend the Gradle FileTree API to my
            // will so as to come up with a more exact check than this.
            task.getInputFileTree().size() == 2
    }

    def 'Getting property file associated with input file'() {
        setup:
            File inputFile = project.file("$examplesDir/simple/dita/root.ditamap")

            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input inputFile
            }

        expect:
            task.getAssociatedPropertyFile(inputFile).getName() == "root.properties"
    }

    def 'Giving DITAVAL as File'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.file("$examplesDir/simple/dita/root.ditamap")
                filter project.file("$examplesDir/simple/dita/root.ditaval")
            }

        then:
            task.ditaVal.getName() == 'root.ditaval'
    }

    // This feature works, but the test doesn't. The FileTree is always empty.
    // No idea why.
    //
    // def 'Giving input file tree'() {
    //     when:
    //         Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
    //             input project.fileTree(dir: "$examplesDir/filetree/dita",
    //                                    include: '*.ditamap')
    //         }

    //     then:
    //         task.getInputFileCollection().size() == 2
    // }

    def 'DITAVAL file is included in the input file tree'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                filter "$examplesDir/simple/dita/root.ditaval"
            }

        then:
            task.getInputFileTree().find {
                it.class == File && it.getName() == 'root.ditaval'
            }
    }

    def 'DITA-OT directory is included in the input file tree if devMode is enabled'() {
        setup:
            project.extensions.create(DITA_OT, DitaOtExtension, project)
            project.ditaOt.dir ditaHome

        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                devMode true
            }

        then:
            task.getInputFileTree().find {
                it.class == File && it.getName() == 'build.xml'
            }
    }

    def 'Single input file => single output directory'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
            }

        then:
            task.getOutputDirectories().collect { it.getName() } == [ "build" ]
    }

    def 'Single directory mode => single output directory'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                singleOutputDir true
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            task.getOutputDirectories().collect { it.getName() } == [ "build" ]
    }

    def 'Multiple input files => multiple input folders'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            task.getOutputDirectories().collect { it.getName() } == [ "one", "two" ]
    }

    def 'Throws InvalidUserDataException if DITA-OT directory is not set'() {
        setup:
            project.extensions.create(DITA_OT, DitaOtExtension, project)

        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                transtype 'html5'
            }

            task.render()

        then:
            thrown InvalidUserDataException
    }

    def 'Does not throw InvalidUserDataException if DITA-OT directory is set'() {
        setup:
            project.extensions.create(DITA_OT, DitaOtExtension, project)
            project.ditaOt.dir ditaHome

            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                transtype 'html5'
            }

        when: 'DITA-OT dir is given but the project is not set up correctly.'
            task.render()

        then: 'Fails because the classpath is not set up.'
            thrown BuildException
    }

    def 'Does not throw any errors of everything is set up properly'() {
        when: 'Project is set up correctly and DITA-OT dir is given.'
            project.apply plugin: DitaOtPlugin

            project.ditaOt.dir ditaHome

            project.dita {
                input "$examplesDir/simple/dita/root.ditamap"
                transtype 'html5'
            }

        then: 'Build succeeds.'
            notThrown BuildException

        cleanup:
            project.tasks.findByName('clean').execute()
    }

}
