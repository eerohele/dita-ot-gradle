package com.github.eerohele

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

    Boolean developmentMode = false
    Boolean singleDirMode = false
    Object inputFiles
    Object ditaVal
    String outputDir = project.buildDir
    String tempDir = getDefaultTempDir()
    Closure props
    String format = DEFAULT_TRANSTYPE

    void devMode(Boolean d) {
        this.developmentMode = d
    }

    void input(Object i) {
        this.inputFiles = i
    }

    void filter(Object f) {
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

    void singleOutputDir(Boolean s) {
        this.singleDirMode = s
    }

    private static File getDefaultTempDir() {
        String tmpdir = System.getProperty('java.io.tmpdir')
        new File("${tmpdir}/dita-ot", System.currentTimeMillis().toString())
    }

    PatternSet getInputFilePatternSet() {
        PatternSet ps = new PatternSet()
        ps.include GlobPatterns.ALL_FILES
        ps.exclude("${FilenameUtils.getBaseName(outputDir)}/" + GlobPatterns.ALL_FILES)
    }

    PatternSet getDitaOtPatternSet() {
        PatternSet ps = new PatternSet()
        ps.include GlobPatterns.ALL_FILES
        ps.exclude 'temp/' + GlobPatterns.ALL_FILES
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
        PatternSet patternSet = getInputFilePatternSet()

        List<FileTree> inputFileTree = (getInputFileCollection().files.collect {
            project.fileTree(it.getParent()).matching(patternSet)
        }).asImmutable()

        List<FileTree> treeWithDitaval = this.ditaVal != null
            ? inputFileTree + project.files(this.ditaVal)
            : inputFileTree

        if (this.developmentMode) {
            treeWithDitaval + project.fileTree(project.ditaOt.home)
                                   .matching(getDitaOtPatternSet()) as Set
        } else {
            treeWithDitaval as Set
        }
    }

    @OutputDirectories
    Set<File> getOutputDirectories() {
        getInputFileCollection().files.collect {
            getOutputDirForFile(it)
        } as Set
    }

    FileCollection getInputFileCollection() {
        project.files(this.inputFiles)
    }

    File getDitaValFile() {
        project.file(this.ditaVal)
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
        File outputDir = new File(this.outputDir)

        if (this.singleDirMode || getInputFileCollection().files.size() == 1) {
            outputDir
        } else {
            new File(outputDir,
                     FilenameUtils.getBaseName(inputFile.getPath()))
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

    @TaskAction
    void render() {
        getInputFileCollection().files.each { File file ->
            File out = getOutputDirForFile(file)

            ant.ant(antfile: "${project.ditaOt.home}/build.xml") {
                property(name: Properties.ARGS_INPUT, location: file.getPath())
                property(name: Properties.OUTPUT_DIR, location: out.getPath())

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

                property(name: Properties.TEMP_DIR, location: this.tempDir)
                property(name: Properties.TRANSTYPE, value: this.format)

                if (this.ditaVal) {
                    property(name: Properties.ARGS_FILTER,
                             location: getDitaValFile().getPath())
                }
            }
        }
    }
}
