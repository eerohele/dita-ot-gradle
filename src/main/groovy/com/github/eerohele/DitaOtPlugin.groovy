package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.file.FileCollection

import org.gradle.api.internal.ClassPathRegistry
import org.gradle.api.internal.DefaultClassPathProvider
import org.gradle.api.internal.DefaultClassPathRegistry
import org.gradle.api.internal.classpath.DefaultModuleRegistry
import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.api.internal.project.antbuilder.DefaultIsolatedAntBuilder
import org.gradle.internal.classloader.DefaultClassLoaderFactory

import org.apache.tools.ant.BuildException
import org.gradle.api.internal.project.IsolatedAntBuilder

class DitaOtPlugin implements Plugin<Project> {
    static final String DITA = 'dita'
    static final String DITA_OT = 'ditaOt'
    static final ConfigObject MESSAGES = new ConfigSlurper().parse(Messages).messages

    private static final ThreadLocal<IsolatedAntBuilder> THREAD_LOCAL_ANT_BUILDER = new ThreadLocal<IsolatedAntBuilder>()

    FileCollection getClasspath(Project project) {
        project.fileTree(dir: project.ditaOt.home).matching {
            include(
                'resources/',
                'lib/**/*.jar',
                'plugins/org.dita.pdf2/lib/fo.jar',
                'plugins/org.dita.pdf2/build/libs/fo.jar'
            )

            exclude(
                'lib/ant-launcher.jar',
                'lib/ant.jar'
            )
        }
    }

    IsolatedAntBuilder makeAntBuilder(FileCollection classpath) {
        if (classpath == null) {
            throw new BuildException(MESSAGES.classpathError)
        }

        ModuleRegistry moduleRegistry = new DefaultModuleRegistry()
        ClassPathRegistry registry = new DefaultClassPathRegistry(new DefaultClassPathProvider(moduleRegistry))
        DefaultIsolatedAntBuilder builder = new DefaultIsolatedAntBuilder(registry, new DefaultClassLoaderFactory())

        builder.execute {
            classpath*.toURI()*.toURL()*.each {
                antProject.getClass().getClassLoader().addURL(it)
            }
        }

        builder
    }

    IsolatedAntBuilder getAntBuilder(FileCollection classpath) {
        IsolatedAntBuilder antBuilder = THREAD_LOCAL_ANT_BUILDER.get();

        if (antBuilder == null) {
            antBuilder = makeAntBuilder(classpath)
            THREAD_LOCAL_ANT_BUILDER.set(antBuilder)
        }

        antBuilder
    }

    @Override
    void apply(Project project) {
        project.apply plugin: 'base'

        project.extensions.create(DITA_OT, DitaOtExtension, project)

        def task = project.task(
            DITA,
            type: DitaOtTask,
            group: 'Documentation',
            description: 'Publishes DITA documentation with DITA Open Toolkit.'
        )

        System.setProperty('java.awt.headless', 'true')

        project.afterEvaluate {
            task.antBuilder = getAntBuilder(getClasspath(project))
        }
    }
}
