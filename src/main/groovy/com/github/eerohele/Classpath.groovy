package com.github.eerohele

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class Classpath {
    static FileCollection pluginClasspath(Project project, File ditaHome) {
        if (ditaHome == null) {
            throw new GradleException("You must specify the DITA-OT directory (ditaOt.dir) before you can retrieve the plugin classpath.")
        }

        File plugins = [
                new File(ditaHome, 'config/plugins.xml'),
                new File(ditaHome, 'resources/plugins.xml')
        ].find { it.exists() }

        if (plugins == null) {
            throw new GradleException(
                    """\
                    Can't find DITA-OT plugin XML file.
                    Are you sure you're using a valid DITA-OT directory?""".stripIndent()
            )
        }

        List<String> archives = new XmlSlurper().parse(plugins).plugin.collectMany { plugin ->
            String xmlBase = plugin['@xml:base'].text()
            File pluginDir = new File(plugins.getParent(), xmlBase)
            assert pluginDir.exists()
            plugin.feature.@file.collect { file -> new File(pluginDir.getParent(), file.text()) }
        }

        assert archives != null && archives.size() > 0

        project.files(archives)
    }

    static FileCollection compile(Project project, File ditaHome) {
        System.setProperty("logback.configurationFile", "${ditaHome}/config/logback.xml")

        project.fileTree(dir: ditaHome).matching {
            include(
                    'config/',
                    'resources/',
                    'lib/**/*.jar'
            )

            exclude(
                    'lib/ant-launcher.jar',
                    'lib/ant.jar'
            )
        } + pluginClasspath(project, ditaHome)
    }
}
