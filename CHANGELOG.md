# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## 0.7.1 - 2020-01-12
- Resolve relative temp dir against project root dir #25

## 0.7.0 - 2020-01-07
- Resolve relative output dir against project root dir #24

## 0.6.0 – 2019-11-12
- Allow setting DITA-OT location in execution phase #14, #19

  This lets you retrieve DITA-OT in the same Gradle build that uses it to
  publish things.

- Migrate documentation into README.md
- Overhaul examples 

## 0.5.0 - 2017-11-07
- Add support for overriding/augmenting DITA-OT classpath #10

## 0.4.2 - 2017-10-29
- Exclude Gradle cache files from up-to-date check #13

## 0.4.2 - 2017-10-17
- Fix DITA-OT 3 and Gradle 4 compatibility issues #12

## 0.4.1 - 2016-01-06
- Fix Apache FOP performance degradation with DITA-OT 2.4.x

## 0.4.0 - 2016-07-04
- Fix support for Gradle 2.14
- Add support for automatically installing DITA-OT plugins #8

## 0.3.1 — 2016-04-29
- Fix support for Gradle 2.13
- Improve input file detection logic
- Fix support for scenarios where DITA-OT libraries aren't yet available in the
  project evaluation phase.

## 0.3.0 — 2016-03-11
- Add support for multiple transtypes
- Fix classpath conflicts and update classpath compilation strategy

## 0.2.1 – 2016-01-22
- Fix Java 7 compatibility

## 0.2.0 — 2015-12-04
- Add useAssociatedFilter property
- Ignore `.gradle` directory in up-to-date check
- Run Ant in headless mode (see [dita-ot/dita-ot#2140](https://github.com/dita-ot/dita-ot/issues/2140)).

## 0.1.0 — 2015-10-31
- Initial release
