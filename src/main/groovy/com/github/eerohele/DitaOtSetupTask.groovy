package com.github.eerohele

import org.gradle.api.DefaultTask
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
        Options.ditaOt = project.ditaOt.dir = project.file(dir)
    }

    @SuppressWarnings('ConfusingMethodName')
    void classpath(Object... classpath) {
        this.classpath = project.files(classpath)
    }

    FileTree getDefaultClasspath(File ditaHome) {
        Classpath.compile(ditaHome).getAsFileTree()
    }

    FileTree getDefaultClasspath(Project project) {
        Classpath.compile(project, project.ditaOt.dir).getAsFileTree()
    }

    FileTree getPluginClasspath(File ditaHome) {
        Classpath.pluginClasspath(ditaHome).getAsFileTree()
    }

    FileTree getPluginClasspath(Project project) {
        Classpath.pluginClasspath(project.ditaOt.dir).getAsFileTree()
    }

    @TaskAction
    void install() {
        if (this.plugins != null && this.dir != null) {
            AntBuilderAssistant.getAntBuilder(getDefaultClasspath(this.dir)).execute {
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
}
