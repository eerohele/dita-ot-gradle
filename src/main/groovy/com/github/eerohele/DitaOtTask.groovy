package com.github.eerohele

import org.apache.commons.io.FilenameUtils as FilenameUtils

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

import org.gradle.api.internal.project.IsolatedAntBuilder

class DitaOtTask extends DefaultTask {
    Options options = new Options()

    DitaOtTask() {
        super()
        this.options.output = project.buildDir
    }

    void devMode(Boolean d) {
        this.options.devMode = d
    }

    void input(Object i) {
        this.options.input = i
    }

    void filter(Object f) {
        this.options.filter = f
    }

    void output(String o) {
        this.options.output = o
    }

    void temp(String t) {
        this.options.temp = t
    }

    void properties(Closure p) {
        this.options.properties = p
    }

    void transtype(String... t) {
        this.options.transtype = t
    }

    void singleOutputDir(Boolean s) {
        this.options.singleOutputDir = s
    }

    void useAssociatedFilter(Boolean a) {
        this.options.useAssociatedFilter = a
    }

    /** Get input files for up-to-date check.
     *
     * By default, all files under all input directories are included in the
     * up-to-date check, apart from the build directory (TODO: that check
     * should be made more robust).
     *
     * If devMode is true, the DITA-OT directory is also checked. That's useful
     * for stylesheet developers who don't want to use --rerun-tasks every time
     * they make a change to the DITA-OT plugin they're developing.
     *
     * @since 0.1.0
     */
    @InputFiles
    @SkipWhenEmpty
    Set<FileTree> getInputFileTree() {
        Set<FileTree> inputFiles = getInputFiles().collect {
            project.fileTree(dir: it.getParent()).exclude {
                   it == project.getGradle().startParameter.getProjectCacheDir() ||
                   it == project.file(this.options.output)
            }
        } as Set

        this.options.filter && inputFiles << project.files(this.options.filter)

        if (this.options.devMode) {
            File ditaHome = project.ditaOt.home

            inputFiles + project.fileTree(dir: ditaHome).exclude {
                it == new File(ditaHome, 'temp')
            }
        } else {
            inputFiles
        }
    }

    @OutputDirectories
    Set<File> getOutputDirectories() {
        getInputFiles().collect { file ->
            this.options.transtype.collect {
                getOutputDirectory(file, it)
            }
        }.flatten() as Set
    }

    FileCollection getInputFiles() {
        project.files(this.options.input)
    }

    File getDitavalFile(File inputFile) {
        if (this.options.filter) {
            project.file(this.options.filter)
        } else {
            getAssociatedFile(inputFile, FileExtensions.DITAVAL)
        }
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
     * @param transtype DITA transtype.
     * @since 0.1.0
     */
    File getOutputDirectory(File inputFile, String transtype) {
        File baseOutputDir = null

        if (this.options.singleOutputDir || getInputFiles().size() == 1) {
            baseOutputDir = new File(this.options.output)
        } else {
            String basename = FilenameUtils.getBaseName(inputFile.getPath())
            baseOutputDir = new File(this.options.output, basename)
        }

        if (transtype && this.options.transtype.size() > 1) {
            new File(baseOutputDir, transtype)
        } else {
            baseOutputDir
        }
    }

    /** Get a file "associated" with a given file.
     *
     * An "associated" file is a file in the same directory as the
     * input file that has the exact same basename but with the given extension.
     *
     * Example: if the input file is `subdir/root.ditamap` and the given
     * extension is ".properties", the associated file is
     *
     *`subdir/root.properties`.
     *
     * @param inputFile Input file.
     * @param extension The extension of the associated file.
     * @since 0.2.0
     */
    static File getAssociatedFile(File inputFile, String extension) {
        String absPath = inputFile.getAbsolutePath()
        String dirname = FilenameUtils.getFullPathNoEndSeparator(absPath)
        String basename = FilenameUtils.getBaseName(absPath)

        new File(FilenameUtils.concat(dirname, basename) + extension)
    }

    IsolatedAntBuilder antBuilder

    @TaskAction
    void render() {
        File ditaHome = project.ditaOt.home

        if (ditaHome == null) {
            throw new InvalidUserDataException(DitaOtPlugin.MESSAGES.ditaHomeError)
        }

        antBuilder.execute {
            getInputFiles().each { File inputFile ->
                File associatedPropertyFile = getAssociatedFile(inputFile, FileExtensions.PROPERTIES)

                this.options.transtype.each { String transtype ->
                    File outputDir = getOutputDirectory(inputFile, transtype)

                    ant(antfile: new File(ditaHome, 'build.xml')) {
                        property(name: Properties.ARGS_INPUT, location: inputFile.getPath())
                        property(name: Properties.OUTPUT_DIR, location: outputDir.getPath())

                        if (this.options.properties) {
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
                            this.options.properties.delegate = ant
                            this.options.properties.call()
                        }

                        property file: associatedPropertyFile

                        property name: Properties.TEMP_DIR, location: this.options.temp
                        property name: Properties.TRANSTYPE, value: transtype

                        if (this.options.filter || this.options.useAssociatedFilter) {
                            property(name: Properties.ARGS_FILTER,
                                     location: getDitavalFile(inputFile).getPath())
                        }
                    }
                }
            }
        }
    }
}
