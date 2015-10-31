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

    private static final String ROOT_DITAMAP = 'root.ditamap'
    private static final String ROOT_DITAVAL = 'root.ditaval'
    private static final String DEFAULT_TRANSTYPE = 'xhtml'

    Project project

    String ditaHome
    File testRootDir
    File examplesDir

    Set<File> getInputFiles(Task task) {
        task.getInputFileCollection().getFiles()
    }

    void setup() {
        ditaHome = System.getProperty('dita.home')

        if (!ditaHome || !new File(ditaHome).isDirectory()) {
            throw new InvalidUserDataException('''dita.home system property not
properly set. To run the tests, you need a working DITA-OT installation and you
need to set the dita.home system property to point to that installation.''')
        }

        project = ProjectBuilder.builder().withName('test').build()
        project.configurations.create(DITA)
        testRootDir = new File('.')
        examplesDir = new File(testRootDir, 'examples')
    }

    @SuppressWarnings('MethodName')
    def 'Creating a task'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input ROOT_DITAMAP
                filter ROOT_DITAVAL
                transtype DEFAULT_TRANSTYPE

                properties {
                    property name: 'processing-mode', value: 'strict'
                }
            }

        then:
            task.inputFiles == ROOT_DITAMAP
            task.ditaVal == ROOT_DITAVAL
            task.format == DEFAULT_TRANSTYPE
            task.props != null
    }

    @SuppressWarnings('MethodName')
    def 'Giving single input file as String'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
            }

        then:
            getInputFiles(task).find { it.getName() == ROOT_DITAMAP }
    }

    @SuppressWarnings('MethodName')
    def 'Giving single input file as File'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.file("$examplesDir/simple/dita/root.ditamap")
            }

        then:
            getInputFiles(task).find { it.getName() == ROOT_DITAMAP }
    }

    @SuppressWarnings('MethodName')
    def 'Giving multiple input files'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            getInputFiles(task)*.getName() == ['one.ditamap', 'two.ditamap']
    }

    @SuppressWarnings('MethodName')
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

    @SuppressWarnings('MethodName')
    def 'Getting property file associated with input file'() {
        setup:
            File inputFile = project.file("$examplesDir/simple/dita/root.ditamap")

            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input inputFile
            }

        expect:
            task.getAssociatedPropertyFile(inputFile).getName() == 'root.properties'
    }

    @SuppressWarnings('MethodName')
    def 'Giving DITAVAL as File'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.file("$examplesDir/simple/dita/root.ditamap")
                filter project.file("$examplesDir/simple/dita/root.ditaval")
            }

        then:
            task.ditaVal.getName() == ROOT_DITAVAL
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

    @SuppressWarnings('MethodName')
    def 'DITAVAL file is included in the input file tree'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                filter "$examplesDir/simple/dita/root.ditaval"
            }

        then:
            task.getInputFileTree().find {
                it.class == File && it.getName() == ROOT_DITAVAL
            }
    }

    @SuppressWarnings('MethodName')
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

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def 'Single input file => single output directory'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
            }

        then:
            task.getOutputDirectories()*.getName() == [ 'build' ]
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    @SuppressWarnings('DuplicateListLiteral')
    def 'Single directory mode => single output directory'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                singleOutputDir true
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            task.getOutputDirectories()*.getName() == [ 'build' ]
    }

    @SuppressWarnings('MethodName')
    def 'Multiple input files => multiple input folders'() {
        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input project.files("$examplesDir/multi/one/one.ditamap",
                                    "$examplesDir/multi/two/two.ditamap")
            }

        then:
            task.getOutputDirectories()*.getName() == [ 'one', 'two' ]
    }

    @SuppressWarnings('MethodName')
    def 'Throws InvalidUserDataException if DITA-OT directory is not set'() {
        setup:
            project.extensions.create(DITA_OT, DitaOtExtension, project)

        when:
            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                transtype DEFAULT_TRANSTYPE
            }

            task.render()

        then:
            thrown InvalidUserDataException
    }

    @SuppressWarnings('MethodName')
    def 'Does not throw InvalidUserDataException if DITA-OT directory is set'() {
        setup:
            project.extensions.create(DITA_OT, DitaOtExtension, project)
            project.ditaOt.dir ditaHome

            Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
                input "$examplesDir/simple/dita/root.ditamap"
                transtype DEFAULT_TRANSTYPE
            }

        when: 'DITA-OT dir is given but the project is not set up correctly.'
            task.render()

        then: 'Fails because the classpath is not set up.'
            thrown BuildException
    }

    @SuppressWarnings('MethodName')
    def 'Does not throw any errors of everything is set up properly'() {
        when: 'Project is set up correctly and DITA-OT dir is given.'
            project.apply plugin: DitaOtPlugin

            project.ditaOt.dir ditaHome

            project.dita {
                input "$examplesDir/simple/dita/root.ditamap"
                transtype DEFAULT_TRANSTYPE
            }

        then: 'Build succeeds.'
            notThrown BuildException

        cleanup:
            project.tasks.findByName('clean').execute()
    }

}
