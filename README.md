# DITA-OT Gradle Plugin

A [Gradle] plugin for publishing DITA documents with [DITA Open Toolkit].

## Use

In your Gradle build script (`build.gradle`), add something like this:

```gradle
plugins {
    id 'com.github.eerohele.dita-ot-gradle' version '0.6.0'
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

## Features

- After the first build, (much) faster than running DITA-OT directly, thanks to the [Gradle Daemon].
- Easy to configure.
- Versatile: publish [multiple documents at once](https://github.com/eerohele/dita-ot-gradle/tree/master/examples/filetree).
- Incremental builds: only build DITA documents that have changed.
- Continuous builds: automatically republish your document when it changes (Gradle's `--continuous` option).

## Examples

See the [`examples`](https://github.com/eerohele/dita-ot-gradle/tree/master/examples) directory in this repository.

You're most welcome to contribute improvements on the current set of examples or entirely new examples.
## Options

| Type | Option | Description |
| ---- | ------ | ----------- |
| `String` or `File` | `input` | The input file. |
| `String` or `File` | `output` | The output directory. Default: `build`. |
| `String...` |	`transtype` | One or more formats to publish into. |
| `String` or `File` | `filter` | Path to DITAVAL file to use for publishing. |
| `Boolean` | `singleOutputDir` | Multiple input files ➞ single output directory. Default: `false`. |
| `Boolean` |	`useAssociatedFilter` |	For every input file, use DITAVAL file in same directory with same basename. Default: `false`. |

## License

The DITA-OT Gradle Plugin is licensed for use under the [Apache License 2.0].

[Apache License 2.0]: https://www.apache.org/licenses/LICENSE-2.0.html
[DITA Open Toolkit]: https://www.dita-ot.org
[Gradle]: https://gradle.org
[Gradle Daemon]: https://docs.gradle.org/current/userguide/gradle_daemon.html
