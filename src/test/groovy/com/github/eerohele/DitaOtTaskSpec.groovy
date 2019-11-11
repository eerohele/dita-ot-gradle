package com.github.eerohele

import org.gradle.api.file.FileCollection
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.InvalidUserDataException
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import org.apache.tools.ant.BuildException

import spock.lang.Specification

class DitaOtTaskSpec extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File settingsFile

    private static final String DITA = 'dita'
    private static final String DITA_OT = 'ditaOt'

    private static final String ROOT_DITAMAP = 'root.ditamap'
    private static final String ROOT_DITAVAL = 'root.ditaval'
    private static final String DEFAULT_TRANSTYPE = 'html5'

    Project project

    String ditaHome
    String examplesDir

    Set<File> getInputFiles(Task task) {
        task.getInputFiles().getFiles()
    }

    void setup() {
        ditaHome = System.getProperty('dita.home').replace('\\', '/')

        buildFile = testProjectDir.newFile('build.gradle')
        settingsFile = testProjectDir.newFile('settings.gradle')

        if (!ditaHome || !new File(ditaHome).isDirectory()) {
            throw new InvalidUserDataException('''dita.home system property not
properly set. To run the tests, you need a working DITA-OT installation and you
need to set the dita.home system property to point to that installation.''')
        }

        examplesDir = System.getProperty('examples.dir').replace('\\', '/')

        assert examplesDir != null

        project = ProjectBuilder.builder().withName('test').build()
        project.configurations.create(DITA)
    }

    @SuppressWarnings(['MethodName', 'DuplicateStringLiteral'])
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
        task.options.input == ROOT_DITAMAP
        task.options.filter == ROOT_DITAVAL
        task.options.transtype == [DEFAULT_TRANSTYPE]
        task.options.properties != null
    }

    @SuppressWarnings(['MethodName', 'DuplicateStringLiteral', 'DuplicateListLiteral'])
    def 'Using multiple transtypes'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input ROOT_DITAMAP
            transtype 'xhtml', 'pdf', 'html5', 'troff'
        }

        then:
        task.options.transtype == ['xhtml', 'pdf', 'html5', 'troff']
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

    @SuppressWarnings(['MethodName', 'DuplicateStringLiteral', 'DuplicateListLiteral'])
    def 'Giving single input file and multiple transtypes'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.file("$examplesDir/simple/dita/root.ditamap")
            transtype 'html5', 'pdf'
        }

        then:
        task.getOutputDirectories()*.getName() == ['html5', 'pdf']
    }

    @SuppressWarnings('MethodName')
    def 'Giving multiple input files'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.files("$examplesDir/multi-project/one/one.ditamap",
                    "$examplesDir/multi-project/two/two.ditamap")
        }

        then:
        getInputFiles(task)*.getName() == ['one.ditamap', 'two.ditamap']
    }

    @SuppressWarnings(['MethodName', 'DuplicateStringLiteral', 'DuplicateListLiteral'])
    def 'Giving multiple input files and multiple transtypes'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.files("$examplesDir/multi-project/one/one.ditamap",
                    "$examplesDir/multi-project/two/two.ditamap")
            transtype 'html5', 'pdf'
        }

        then:
        task.getOutputDirectories().collect {
            File parent = new File(it.getParent())
            new File(parent.getName(), it.getName()).getPath().replace('\\', '/')
        } == ['one/html5', 'one/pdf', 'two/html5', 'two/pdf']
    }

    @SuppressWarnings('MethodName')
    def 'Includes containing directories in up-to-date check'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.files("$examplesDir/multi-project/one/one.ditamap",
                    "$examplesDir/multi-project/two/two.ditamap")
        }

        then:
        Set<File> inputFiles = task.getInputFileTree().files.flatten()
        inputFiles.contains(new File("$examplesDir/multi-project/one/one.ditamap"))
        inputFiles.contains(new File("$examplesDir/multi-project/two/two.ditamap"))
    }

    @SuppressWarnings('MethodName')
    def 'Getting file associated with input file'() {
        setup:
        File inputFile = project.file("$examplesDir/simple/dita/root.ditamap")

        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input inputFile
        }

        expect:
        task.getAssociatedFile(inputFile, '.properties').getName() == 'root.properties'
    }

    @SuppressWarnings('MethodName')
    def 'Giving DITAVAL as File'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.file("$examplesDir/simple/dita/root.ditamap")
            filter project.file("$examplesDir/simple/dita/root.ditaval")
        }

        then:
        task.options.filter.getName() == ROOT_DITAVAL
    }

    @SuppressWarnings('MethodName')
    def 'Giving input file tree'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.fileTree(dir: "$examplesDir/filetree/dita",
                    include: '*.ditamap')
        }

        then:
        Set<File> inputFiles = task.getInputFileTree().files.flatten()
        inputFiles.contains(new File("$examplesDir/filetree/dita/one.ditamap"))
        inputFiles.contains(new File("$examplesDir/filetree/dita/two.ditamap"))
    }

    @SuppressWarnings('MethodName')
    def 'DITAVAL file is included in the input file tree'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input "$examplesDir/simple/dita/root.ditamap"
            filter "$examplesDir/simple/dita/root.ditaval"
        }

        then:
        task.getInputFileTree().find {
            it.contains(new File("$examplesDir/simple/dita/root.ditaval"))
        }
    }

    @SuppressWarnings('MethodName')
    def 'DITAVAL file is included in the input file tree when outside root map directory'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input "$examplesDir/simple/dita/root.ditamap"
            filter "$examplesDir/filetree/dita/two.ditaval"
        }

        then:
        task.getInputFileTree().find {
            it.contains(new File("$examplesDir/filetree/dita/two.ditaval"))
        }
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def 'Using associated DITAVAL file'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input "$examplesDir/simple/dita/root.ditamap"
            useAssociatedFilter true
        }

        then:
        File inputFile = task.getInputFiles().files[0]
        task.getDitavalFile(inputFile) == new File("$examplesDir/simple/dita/root.ditaval")
    }

    @SuppressWarnings('MethodName')
    def 'DITA-OT directory is included in the input file tree if devMode is enabled'() {
        setup:
        project.tasks.create(name: DITA_OT, type: DitaOtSetupTask) {
            dir ditaHome
        }

        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input "$examplesDir/simple/dita/root.ditamap"
            devMode true
        }

        then:
        task.getInputFileTree().contains(new File(ditaHome, 'build.xml'))

        and:
        !task.getInputFileTree().contains(new File(ditaHome, 'lib/org.dita.dost.platform/plugin.properties'))

        and:
        !task.getInputFileTree().contains(new File(ditaHome, 'lib/dost-configuration.jar'))
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def 'DITA-OT directory is not included in the input file tree if devMode is disabled'() {
        setup:
        project.tasks.create(name: DITA_OT, type: DitaOtSetupTask) {
            dir ditaHome
        }

        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input "$examplesDir/simple/dita/root.ditamap"
            devMode false
        }

        then:
        !task.getInputFileTree().contains(new File(ditaHome, 'build.xml'))
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def 'Allows overriding and augmenting the default classpath'() {
        when:
        project.tasks.create(name: DITA_OT, type: DitaOtSetupTask) {
            dir ditaHome

            classpath getDefaultClasspath(project).matching {
                exclude('**/Saxon*.jar')
            } + project.file('foo.jar')
        }

        then:
        FileCollection classpath = project.tasks.getByName(DITA_OT).getProperties().get('classpath')
        classpath.getFiles().findAll { it.getName().matches(/.*Saxon.*\.jar/) }.isEmpty()
        classpath.contains(project.file('foo.jar'))
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def 'Single input file => single output directory'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input "$examplesDir/simple/dita/root.ditamap"
        }

        then:
        task.getOutputDirectories()*.getName() == ['build']
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    @SuppressWarnings('DuplicateListLiteral')
    def 'Single directory mode => single output directory'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            singleOutputDir true
            input project.files("$examplesDir/multi-project/one/one.ditamap",
                    "$examplesDir/multi-project/two/two.ditamap")
        }

        then:
        task.getOutputDirectories()*.getName() == ['build']
    }

    @SuppressWarnings('MethodName')
    def 'Multiple input files => multiple input folders'() {
        when:
        Task task = project.tasks.create(name: DITA, type: DitaOtTask) {
            input project.files("$examplesDir/multi-project/one/one.ditamap",
                    "$examplesDir/multi-project/two/two.ditamap")
        }

        then:
        task.getOutputDirectories()*.getName() == ['one', 'two']
    }

    @SuppressWarnings('MethodName')
    def 'Build fails if DITA-OT directory is not set'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
                plugins {
                    id 'com.github.eerohele.dita-ot-gradle'
                }
                
                dita {
                    input '$examplesDir/simple/dita/root.ditamap'
                    transtype 'html5'
                }
            """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('dita')
                .buildAndFail()

        then:
        result.task(':dita').outcome == FAILED
    }

    @SuppressWarnings('MethodName')
    def 'Works when DITA-OT dir is defined inside the "dita" closure'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
                    plugins {
                        id 'com.github.eerohele.dita-ot-gradle'
                    }

                    dita {
                        ditaOt '$ditaHome'
                        input '$examplesDir/simple/dita/root.ditamap'
                        transtype 'html5'
                    }
            """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('dita')
                .build()

        then:
        result.task(':dita').outcome == SUCCESS
        new File("${testProjectDir.root}/build/topic1.html").exists()
        notThrown BuildException
    }

    @SuppressWarnings('MethodName')
    def 'Works when DITA-OT dir is defined inside the "ditaOt" closure'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
            plugins {
                id 'com.github.eerohele.dita-ot-gradle'
            }
            
            ditaOt {
                dir '$ditaHome'
            }
            
            dita {
                input '$examplesDir/simple/dita/root.ditamap'
                transtype 'html5'
            }
        """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('dita')
                .build()

        then:
        result.task(':dita').outcome == SUCCESS
        new File("${testProjectDir.root}/build/topic1.html").exists()
        notThrown BuildException
    }

    @SuppressWarnings('MethodName')
    def 'Installing plugins inside "ditaOt" (deprecated)'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
                plugins {
                    id 'com.github.eerohele.dita-ot-gradle'
                }

                ditaOt {
                    dir '$ditaHome'
                    plugins 'https://github.com/jelovirt/org.lwdita/releases/download/2.2.0/org.lwdita-2.2.0.zip'
                }
                
                dita {
                    input '$examplesDir/simple/dita/root.ditamap'
                    transtype 'markdown'
                }
                """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('dita')
                .build()

        then:
        result.task(':dita').outcome == SUCCESS
        new File("${testProjectDir.root}/build/topic1.md").exists()
        notThrown BuildException
    }

    @SuppressWarnings('MethodName')
    def 'Specifying no inputs skips the task'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
                plugins {
                    id 'com.github.eerohele.dita-ot-gradle'
                }
                
                dita {
                    ditaOt '$ditaHome'
                    transtype 'html5'
                }
                """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('dita')
                .build()

        then:
        result.task(':dita').outcome == NO_SOURCE
    }

    /*
    @SuppressWarnings('MethodName')
    def 'Multiple publishing tasks'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
                plugins {
                    id 'com.github.eerohele.dita-ot-gradle'
                }

                import com.github.eerohele.DitaOtTask
                
                task web(type: DitaOtTask) {
                    ditaOt '$ditaHome'
                    input '$examplesDir/multi-task/dita/root.ditamap'
                    transtype 'html5'
                    filter '$examplesDir/multi-task/dita/a.ditaval'
                }
                
                task pdf(type: DitaOtTask) {
                    ditaOt '$ditaHome'
                    input '$examplesDir/multi-task/dita/root.ditamap'
                    transtype 'pdf'
                    filter '$examplesDir/multi-task/dita/b.ditaval'
                }
                """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('web', 'pdf')
                .build()

        then:
        result.task(':web').outcome == SUCCESS
        result.task(':pdf').outcome == SUCCESS
        new File("${testProjectDir.root}/build/root.pdf").exists()
        new File("${testProjectDir.root}/build/topic1.html").exists()
        notThrown BuildException
    }
    */

    @SuppressWarnings('MethodName')
    def 'Filtering with DITAVAL'() {
        given:
        settingsFile << "rootProject.name = 'dita-test'"

        buildFile << """
                plugins {
                    id 'com.github.eerohele.dita-ot-gradle'
                }

                dita {
                    ditaOt '$ditaHome'
                    input '$examplesDir/simple/dita/root.ditamap'
                    filter '$examplesDir/simple/dita/root.ditaval'
                    transtype 'html5'
                }
                """

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('dita')
                .build()

        then:
        result.task(':dita').outcome == SUCCESS
        Document doc = Jsoup.parse(new File("${testProjectDir.root}/build/topic1.html"), 'UTF-8')
        doc.select("p").first().outerHtml() == '<p class="p">baz </p>'
    }
}
