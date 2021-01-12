# DITA-OT Gradle Plugin

A [Gradle] plugin for publishing DITA documents with [DITA Open Toolkit].

## Use

In your Gradle build script (`build.gradle`), add something like this:

```gradle
plugins {
    id 'com.github.eerohele.dita-ot-gradle' version '0.7.1'
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

To get started, see [the simple example](https://github.com/eerohele/dita-ot-gradle/tree/master/examples/simple) buildfile.

For more examples, see the [`examples`](https://github.com/eerohele/dita-ot-gradle/tree/master/examples) directory in this repository.

You're most welcome to contribute improvements on the current set of examples or entirely new examples.

## Downloading DITA Open Toolkit

You can use the [Gradle Download Task](https://github.com/michel-kraemer/gradle-download-task) to download DITA-OT and
use the downloaded version in your build. See the [`download` example](https://github.com/eerohele/dita-ot-gradle/blob/master/examples/download/build.gradle) for an example.

## Options

| Type | Option | Description |
| ---- | ------ | ----------- |
| `String` or `File` | `input` | The input file. |
| `String` or `File` | `output` | The output directory. Default: `build`. |
| `String...` |	`transtype` | One or more formats to publish into. |
| `String` or `File` | `filter` | Path to DITAVAL file to use for publishing. |
| `Boolean` | `singleOutputDir` | Multiple input files ➞ single output directory. Default: `false`. |
| `Boolean` |	`useAssociatedFilter` |	For every input file, use DITAVAL file in same directory with same basename. Default: `false`. |

## Passing Ant properties to DITA-OT

To pass an Ant property to DITA-OT, use the `properties` block. For example:

```groovy
// Give DITA-OT additional parameters.
//
// For a list of the parameters DITA-OT understands, see:
// http://www.dita-ot.org/2.1/parameters/
properties {
    property(name: 'processing-mode', value: 'strict')
}
```

If your Ant properties are paths (such as `args.cssroot`), you need to use absolute paths. If the path is under the same directory as `build.gradle`, you can use the `projectDir` variable:

```groovy
properties {
    // Note the double quotes around the value; with single quotes,
    // the projectDir variable won't be expanded.
    property(name: 'args.cssroot', value: "${projectDir}/my/awesome/path")
}
```

## License

The DITA-OT Gradle Plugin is licensed for use under the [Apache License 2.0].

[Apache License 2.0]: https://www.apache.org/licenses/LICENSE-2.0.html
[DITA Open Toolkit]: https://www.dita-ot.org
[Gradle]: https://gradle.org
[Gradle Daemon]: https://docs.gradle.org/current/userguide/gradle_daemon.html
