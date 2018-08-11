# DITA-OT Gradle Plugin [![Build Status](https://travis-ci.org/eerohele/dita-ot-gradle.svg?branch=master)](https://travis-ci.org/eerohele/dita-ot-gradle)

A [Gradle] plugin for publishing DITA documents with [DITA Open Toolkit].

## Use

In your Gradle build script (`build.gradle`), add something like this:

```gradle
plugins {
    id 'com.github.eerohele.dita-ot-gradle' version '0.5.0'
}

// Publish my.ditamap into the HTML5 output format.
dita {
    ditaOt '/path/to/my/dita-ot/directory'
    input 'my.ditamap'
    transtype 'html5'
}
```

Then, in the directory where you saved the file, run:

```bash
gradle dita
```

By default, the output appears in the `build` subdirectory.

## Examples

For more examples, see the `examples` directory in this repository.

## Features

- (Much) faster than running DITA-OT directly after the first build, thanks to the [Gradle Daemon].
- Simple API.
- Incremental builds: only build DITA documents that have changed.
- Continuous builds: automatically republish your document when it changes.

## License

The DITA-OT Gradle Plugin is licensed for use under the [Apache License 2.0].

[Apache License 2.0]: https://www.apache.org/licenses/LICENSE-2.0.html
[DITA Open Toolkit]: https://www.dita-ot.org
[Gradle]: https://gradle.org
[Gradle Daemon]: https://docs.gradle.org/current/userguide/gradle_daemon.html
