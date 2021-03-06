package com.github.eerohele

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

@Deprecated
class DitaOtSetupTask extends DefaultTask {
    @InputFiles
    FileCollection classpath
    @InputFile
    File dir
    @Internal
    List<Object> plugins

    @Deprecated
    @SuppressWarnings('ConfusingMethodName')
    void plugins(Object... plugins) {
        logger.warn('The "ditaOt" task is deprecated.')
        this.plugins = plugins
    }

    @Deprecated
    @SuppressWarnings('ConfusingMethodName')
    void dir(Object dir) {
        project.ditaOt.dir = project.file(dir)
    }

    @Deprecated
    @SuppressWarnings('ConfusingMethodName')
    void classpath(Object... classpath) {
        logger.warn('The "ditaOt" task is deprecated.')
        this.classpath = project.files(classpath)
    }

    @Deprecated
    FileTree getDefaultClasspath(File ditaHome) {
        logger.warn('The "ditaOt" task is deprecated.')
        Classpath.compile(ditaHome).getAsFileTree()
    }

    @Deprecated
    FileTree getDefaultClasspath(Project project) {
        logger.warn('The "ditaOt" task is deprecated.')
        Classpath.compile(project, project.ditaOt.dir).getAsFileTree()
    }

    @Deprecated
    FileTree getPluginClasspath(File ditaHome) {
        logger.warn('The "ditaOt" task is deprecated.')
        Classpath.pluginClasspath(ditaHome).getAsFileTree()
    }

    @Deprecated
    FileTree getPluginClasspath(Project project) {
        logger.warn('The "ditaOt" task is deprecated.')
        Classpath.pluginClasspath(project.ditaOt.dir).getAsFileTree()
    }

    @TaskAction
    @Deprecated
    void install() {
        logger.warn('The "ditaOt" task is deprecated.')
        if (this.plugins != null && this.dir != null) {
            Ant.getBuilder(getDefaultClasspath(this.dir)).execute {
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
