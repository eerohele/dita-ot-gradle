package com.github.eerohele

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.util.PatternSet
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree

import org.apache.commons.io.FilenameUtils as FilenameUtils

class DitaOtTask extends DefaultTask {
    static final DEFAULT_TRANSTYPE = 'html5'

    String ditaHome
    Object inputFiles
    String ditaVal
    String outputDir = project.buildDir
    String tempDir = getDefaultTempDir()
    Closure props
    String format = DEFAULT_TRANSTYPE

    void home(String h) {
        this.ditaHome = h
    }

    void input(Object i) {
        this.inputFiles = i
    }

    void filter(String f) {
        this.ditaVal = f
    }

    void output(String o) {
        this.outputDir = o
    }

    void temp(String t) {
        this.tempDir = t
    }

    void properties(Closure p) {
        this.props = p
    }

    void transtype(String t) {
        this.format = t
    }

    private static File getDefaultTempDir() {
        String tmpdir = System.getProperty("java.io.tmpdir")

        return new File("${tmpdir}/dita-ot",
                        System.currentTimeMillis().toString())
    }

    PatternSet getInputFilePatternSet() {
        def ps = new PatternSet()
        ps.include '**/*'
        ps.exclude "${FilenameUtils.getBaseName(outputDir)}/**/*"
    }

    @InputFiles
    @SkipWhenEmpty
    Set<FileTree> getInputFileTree() {
        PatternSet patternSet = getInputFilePatternSet()

        getInputFileCollection().files.collect {
            project.fileTree(it.getParent()).matching(patternSet)
        } as Set
    }

    @OutputDirectories
    Set<File> getOutputDirectories() {
        getInputFileCollection().files.collect() {
            getOutputDirForFile(it)
        } as Set
    }

    FileCollection getInputFileCollection() {
        project.files(this.inputFiles)
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
     * @since 0.1.0
     */
    File getOutputDirForFile(File inputFile) {
        new File(this.outputDir,
                 FilenameUtils.getBaseName(inputFile.getPath()))
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

    @TaskAction
    void render() {
        if (!this.ditaHome) {
            setDitaHome(project.dita.home)
        }

        getInputFileCollection().files.each { File file ->
            File out = getOutputDirForFile(file)

            ant.ant(antfile: "${getDitaHome()}/build.xml") {
                property(name: Properties.ARGS_INPUT, location: file.getPath())
                property(name: Properties.OUTPUT_DIR, location: out.getPath())
                property(name: Properties.TEMP_DIR, location: this.tempDir)
                property(name: Properties.TRANSTYPE, value: this.format)

                if (this.ditaVal) {
                    property(name: Properties.ARGS_FILTER,
                             location: this.ditaVal)
                }

                if (this.props) {
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
                    this.props.delegate = ant
                    this.props.call()
                }

                property(file: getAssociatedPropertyFile(file).getPath())
            }
        }
    }
}
