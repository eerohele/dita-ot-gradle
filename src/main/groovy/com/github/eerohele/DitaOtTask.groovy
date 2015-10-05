package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileCollection

import org.apache.commons.io.FilenameUtils as FilenameUtils

class DitaOtTask extends DefaultTask {
    FileCollection getInputFiles() {
        project.files(project.ditaOt.input)
    }

    /** Get the output directory for the given DITA map.
     *
     * If the user has given an output directory, use that. Otherwise,
     * use the basename of the input DITA file.
     *
     * Example: if the name input DITA file is `root`, the output directory
     * is `${buildDir}/root`.
     *
     * @param inputFile Input DITA file.
     * @param files The number of input files.
     * @since 0.1.0
     */
    File getOutputDir(File inputFile, int numberOfFiles) {
        File outputDir = new File(project.ditaOt.output)

        if (numberOfFiles == 1) {
            outputDir
        } else {
            new File(outputDir, FilenameUtils.getBaseName(inputFile.getPath()))
        }
    }

    /** Get the associated property file for the given DITA map.
     *
     * An "associated" property file is a file in the same directory as the
     * input DITA file that has the exact same basename but with the
     * .properties extension.
     *
     * Example: if the input DITA file is `subdir/root.ditamap`, the associated
     * property file is `subdir/root.properties`.
     *
     * @param file Input DITA file.
     * @since 0.1.0
     */
    static File getAssociatedPropertyFile(File inputFile) {
        String absPath = inputFile.getAbsolutePath()
        String dirname = FilenameUtils.getFullPathNoEndSeparator(absPath)
        String basename = FilenameUtils.getBaseName(absPath)

        new File(FilenameUtils.concat(dirname, basename) +
                 FileExtensions.PROPERTIES)
    }

    void runDitaOt() {
        Closure properties = project.ditaOt.properties
        FileCollection inputFiles = getInputFiles()

        inputFiles.files.each { File file ->
            File outputDir = getOutputDir(file, inputFiles.size())

            ant.ant(antfile: "${project.ditaOt.home}/build.xml") {
                property(name: Properties.ARGS_INPUT, file.getPath())
                property(name: Properties.OUTPUT_DIR, outputDir.getPath())
                property(name: Properties.TRANSTYPE, project.ditaOt.transtype)
                property(name: Properties.TEMP_DIR, project.ditaOt.temp)

                if (properties) {
                    // Set the Closure delegate to the `ant` property so that
                    // The user can do this:
                    //
                    //   properties {
                    //       property(name: "foo", value: "bar")
                    //   }
                    //
                    // Instead of this:
                    //
                    //   properties {
                    //       ant.property(name: "foo", value: "bar")
                    //   }
                    properties.delegate = ant
                    properties.call()
                }

                property(file: getAssociatedPropertyFile(file).getPath())
            }
        }
    }

    @TaskAction
    void run() {
        // TODO: Investigate the possibility of parallel execution with
        // groovyx.gpars.GParsPool. I tried it, but it causes builds to become
        // unreliable, probably because DITA-OT isn't fully thread-safe.
        runDitaOt()
    }
}
