package com.github.eerohele

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import org.gradle.api.internal.ClassPathRegistry
import org.gradle.api.internal.DefaultClassPathProvider
import org.gradle.api.internal.DefaultClassPathRegistry
import org.gradle.api.internal.classpath.DefaultModuleRegistry
import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.api.internal.project.antbuilder.DefaultIsolatedAntBuilder
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.internal.classloader.DefaultClassLoaderFactory
import org.gradle.internal.installation.CurrentGradleInstallation

import org.gradle.util.GradleVersion

class AntBuilderAssistant {
    private static final ThreadLocal<IsolatedAntBuilder> THREAD_LOCAL_ANT_BUILDER = new ThreadLocal<IsolatedAntBuilder>()

    private static FileCollection getPluginArchives(Project project) {
        File plugins = [
                new File(project.ditaOt.dir, 'config/plugins.xml'),
                new File(project.ditaOt.dir, 'resources/plugins.xml')
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

    private static FileCollection getClasspath(Project project) {
        project.fileTree(dir: project.ditaOt.dir).matching {
            include(
                    'config/',
                    'resources/',
                    'lib/**/*.jar'
            )

            exclude(
                    'lib/ant-launcher.jar',
                    'lib/ant.jar'
            )
        } + getPluginArchives(project)
    }

    protected static IsolatedAntBuilder makeAntBuilder(Project project) {
        FileCollection classpath = getClasspath(project)
        ModuleRegistry moduleRegistry
        DefaultIsolatedAntBuilder antBuilder

        if (classpath == null) {
            throw new GradleException(DitaOtPlugin.MESSAGES.classpathError)
        }

        if (GradleVersion.current() >= GradleVersion.version('2.13')) {
            moduleRegistry = new DefaultModuleRegistry(CurrentGradleInstallation.get())
        } else {
            moduleRegistry = new DefaultModuleRegistry()
        }

        ClassPathRegistry classPathRegistry = new DefaultClassPathRegistry(new DefaultClassPathProvider(moduleRegistry))

        if (GradleVersion.current() > GradleVersion.version('2.13')) {
            antBuilder = new DefaultIsolatedAntBuilder(classPathRegistry, new DefaultClassLoaderFactory(), moduleRegistry)
        } else {
            antBuilder = new DefaultIsolatedAntBuilder(classPathRegistry, new DefaultClassLoaderFactory())
        }

        antBuilder.execute {
            classpath*.toURI()*.toURL()*.each {
                antProject.getClass().getClassLoader().addURL(it)
            }
        }

        antBuilder
    }

    protected static IsolatedAntBuilder getAntBuilder(Project project) {
        IsolatedAntBuilder antBuilder = THREAD_LOCAL_ANT_BUILDER.get()

        if (antBuilder == null) {
            antBuilder = makeAntBuilder(project)
            THREAD_LOCAL_ANT_BUILDER.set(antBuilder)
        }

        antBuilder
    }
}
