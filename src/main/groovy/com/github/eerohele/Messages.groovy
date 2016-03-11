messages {
    javaVersionWarning = '''There\'s a memory leak in Gradle that causes a PermGen error after about 20 runs with the same Gradle daemon.
You might want to consider using Java 8 instead.'''
    classpathError = '''Could not set up the classpath. Does ditaOt.dir point to a working DITA-OT
installation? If yes, please report this error in the GitHub issue tracker.'''
    ditaHomeError = '''DITA Open Toolkit directory not set.
Add a line like this into build.gradle: ditaOt.dir /path/to/your/dita-ot/installation'''
}
