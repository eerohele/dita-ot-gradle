package com.github.eerohele

import org.apache.commons.io.FilenameUtils as FilenameUtils
import org.apache.tools.ant.BuildException

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

import org.gradle.api.internal.project.IsolatedAntBuilder

import javax.inject.Inject

class DitaOtTask extends DefaultTask {
    static final DEFAULT_TRANSTYPE = 'html5'

    Boolean developmentMode = false
    Boolean singleDirMode = false
    Boolean associatedDitaVal = false
    Object inputFiles
    Object ditaVal
    String outputDir = project.buildDir
    String tempDir = getDefaultTempDir()
    Closure props
    List<String> formats = [DEFAULT_TRANSTYPE]

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

    void transtype(String... t) {
        this.formats = t
    }

    void singleOutputDir(Boolean s) {
        this.singleDirMode = s
    }

    void useAssociatedFilter(Boolean a) {
        this.associatedDitaVal = a
    }

    private static File getDefaultTempDir() {
        String tmpdir = System.getProperty('java.io.tmpdir')
        new File("${tmpdir}/dita-ot", System.currentTimeMillis().toString())
    }

    PatternSet getInputFilePatternSet() {
        PatternSet ps = new PatternSet()
        ps.include GlobPatterns.ALL_FILES
        ps.exclude("${FilenameUtils.getBaseName(outputDir)}/" + GlobPatterns.ALL_FILES)
        ps.exclude('.gradle/' + GlobPatterns.ALL_FILES)
    }

    PatternSet getDitaOtPatternSet() {
        PatternSet ps = new PatternSet()
        ps.include GlobPatterns.ALL_FILES
        ps.exclude 'temp/' + GlobPatterns.ALL_FILES
    }

    @InputFiles
    FileCollection ditaOtClasspath

    @Inject
    IsolatedAntBuilder getAntBuilder() {
        throw new UnsupportedOperationException()
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
            ? inputFileTree + project.files(this.ditaVal) : inputFileTree

        if (this.developmentMode) {
            treeWithDitaval + project.fileTree(project.ditaOt.home)
                                   .matching(getDitaOtPatternSet()) as Set
        } else {
            treeWithDitaval as Set
        }
    }

    @OutputDirectories
    Set<File> getOutputDirectories() {
        getInputFileCollection().files.collect { file ->
            this.formats.collect {
                getOutputDirectory(file, it)
            }
        }.flatten() as Set
    }

    FileCollection getInputFileCollection() {
        project.files(this.inputFiles)
    }

    File getDitaValFile(File inputFile) {
        if (this.ditaVal) {
            project.file(this.ditaVal)
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
     * @param outputFormat DITA transtype.
     * @since 0.1.0
     */
    File getOutputDirectory(File inputFile, String outputFormat) {
        File baseOutputDir = null

        if (this.singleDirMode || getInputFileCollection().files.size() == 1) {
            baseOutputDir = new File(this.outputDir)
        } else {
            String basename = FilenameUtils.getBaseName(inputFile.getPath())
            baseOutputDir = new File(this.outputDir, basename)
        }

        if (outputFormat && this.formats.size() > 1) {
            new File(baseOutputDir, outputFormat)
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

    @TaskAction
    void render() {
        File ditaHome = project.ditaOt.home

        if (ditaHome == null) {
            throw new InvalidUserDataException(DitaOtPlugin.MESSAGES.ditaHomeError)
        }

        FileCollection classpath = getDitaOtClasspath()

        if (classpath == null) {
            throw new BuildException(DitaOtPlugin.MESSAGES.classpathError)
        }

        File antfile = new File(ditaHome, 'build.xml')
        List<String> outputFormats = this.formats

        getInputFileCollection().each { File inputFile ->
            File associatedPropertyFile = getAssociatedFile(inputFile, FileExtensions.PROPERTIES)

            antBuilder.withClasspath(classpath).execute {
                // Add every JAR file in the DITA-OT "lib" directory into the Ant
                // class loader.
                URLClassLoader antClassLoader = antProject.getClass().classLoader
                classpath*.toURI()*.toURL().each { antClassLoader.addURL(it) }

                outputFormats.each { String outputFormat ->
                    File outputDir = getOutputDirectory(inputFile, outputFormat)

                    ant(antfile: antfile.getPath()) {
                        property(name: Properties.ARGS_INPUT, location: inputFile.getPath())
                        property(name: Properties.OUTPUT_DIR, location: outputDir.getPath())

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

                        property file: associatedPropertyFile

                        property name: Properties.TEMP_DIR, location: this.tempDir
                        property name: Properties.TRANSTYPE, value: outputFormat

                        if (this.ditaVal || this.associatedDitaVal) {
                            property(name: Properties.ARGS_FILTER,
                                     location: getDitaValFile(inputFile).getPath())
                        }
                    }
                }
            }
        }
    }
}
