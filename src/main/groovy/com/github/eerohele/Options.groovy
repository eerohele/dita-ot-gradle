package com.github.eerohele

class Options {
    static final String DEFAULT_TRANSTYPE = 'html5'

    Boolean devMode = false
    Boolean singleOutputDir = false
    Boolean useAssociatedFilter = false

    File ditaOt
    Object input
    Object filter
    String output
    Object temp = getDefaultTempDir()

    Closure properties
    List<String> transtype = [DEFAULT_TRANSTYPE]

    private static File getDefaultTempDir() {
        String tmpdir = System.getProperty('java.io.tmpdir')
        new File("${tmpdir}/dita-ot", System.currentTimeMillis().toString())
    }
}
