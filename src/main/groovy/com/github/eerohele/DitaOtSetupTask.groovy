package com.github.eerohele

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

class DitaOtSetupTask extends DefaultTask {
    List<Object> plugins
    File dir

    @SuppressWarnings('ConfusingMethodName')
    void plugins(Object... plugins) {
        this.plugins = plugins
    }

    @SuppressWarnings('ConfusingMethodName')
    void dir(Object dir) {
        this.dir = project.file(dir)
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