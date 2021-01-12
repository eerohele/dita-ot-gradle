package com.github.eerohele

import org.gradle.api.file.FileCollection

class Options {
    static final String DEFAULT_TRANSTYPE = 'html5'

    Boolean devMode = false
    Boolean singleOutputDir = false
    Boolean useAssociatedFilter = false

    File ditaOt
    FileCollection classpath
    Object input
    Object filter
    File output
    File temp = getDefaultTempDir()

    Closure properties
    List<String> transtype = [DEFAULT_TRANSTYPE]

    private static File getDefaultTempDir() {
        String tmpdir = System.getProperty('java.io.tmpdir')
        new File("${tmpdir}/dita-ot", System.currentTimeMillis().toString())
    }
}
