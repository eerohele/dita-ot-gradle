package com.github.eerohele

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class DitaOtSetupTask extends DefaultTask {
    FileCollection classpath
    File dir
    List<Object> plugins

    @SuppressWarnings('ConfusingMethodName')
    void plugins(Object... plugins) {
        this.plugins = plugins
    }

    @SuppressWarnings('ConfusingMethodName')
    void dir(Object dir) {
        this.dir = project.file(dir)
        this.classpath == null && (this.classpath = Classpath.forProject(project))
    }

    @SuppressWarnings('ConfusingMethodName')
    void classpath(Object... classpath) {
        this.classpath = project.files(classpath)
    }

    FileTree getDefaultClasspath(Project project) {
        Classpath.forProject(project).getAsFileTree()
    }

    FileTree getPluginClasspath(Project project) {
        Classpath.pluginClasspath(project).getAsFileTree()
    }

    @TaskAction
    void install() {
        if (this.dir == null) {
            throw new InvalidUserDataException(DitaOtPlugin.MESSAGES.ditaHomeError)
        }

        AntBuilderAssistant.getAntBuilder(project).execute {
            this.plugins.each { Object plugin ->
                ant(
                    antfile: new File(this.dir, 'integrator.xml'),
                    target: 'install',
                    useNativeBaseDir: true
                ) {
                    property(name: 'plugin.file', value: plugin.toString())
                }
            }
        }
    }
}