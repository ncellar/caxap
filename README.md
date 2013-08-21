# caxap (v0.1.0) - Sweeten your Java code

caxap is a syntactic macro system working on top of the Java language. It acts
as a source pre-processor.

The system is geared towards maximum expressivity. It allows adding arbitrary
syntax to the Java language, and executing arbitrary Java code at compile-time
to translate the new syntax to plain Java syntax. The system features, among
other things, a quasiquotation system.

WARNING: The system is pretty much alpha, and probably contain huge bugs. But if
you're tempted to give it a spin, I'd love to have your feedback.

"caxap" means "sugar" in russian. It should be pronounced "katchap". (This is
not the correct russian pronunciation, which sounds more like "zehrer" or
something.) The name was chosen because caxap adds syntactic sugar on top of
Java.

## Simple Example

A macro definition:

    macro Unless as Statement : "unless" cond:expression :body
    {
      return `statement[if (!(#cond)) #body]`;
    }

Using the macro:

    unless myPredicate() {
      myMethod();
    }

Expansion:

    if (!(myPredicate()) {
      myMethod();
    }

More complex examples are available in the
[examples](https://github.com/norswap/caxap/tree/master/examples) directory.

## Requirements

caxap requires the Java Development Kit (JDK) version 7. You will also need
Apache Maven if you want to compile from source.

## Installation

You can obtain the latest version of caxap online at
https://github.com/norswap/caxap.

You can either download a [precompiled JAR
file](https://github.com/norswap/caxap/raw/master/prebuilt/caxap-1.0-SNAPSHOT.jar)
or build caxap from source.

Maven artifacts will be provided when caxap reaches version 1.0.0.

## Compiling from Source

From the repository root, execute `mvn package`. The generated JAR file will be
placed under the `target` directory.

## Usage

caxap's user manual is the fourth chapter [my master
thesis](https://github.com/norswap/caxap/raw/master/thesis/thesis.pdf), which is
included in this repository.

The fifth chapter begins by a quick tour of the source code.

## Eclipse Project

The source tree includes an Eclipse project file, as well as my eclipse config.
To make the project file work, you need to define the `M2_REPO` Eclipse
classpath variable to the path of your local maven repository (usually
`~/.m2/repository`). This can be done in Window > Preferences > Java > Build
Path > Classpath Variables.

To regenerate the eclipse .project file, run:

    mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo
    mvn eclipse:eclipse

The `eclipse:add-maven-repo` goal requires an eclipse restart to take effect.

If you want to change the project-specific settings, be sure to run the
following command before commiting:

    git ls-files .settings | tr '\n' '\0' | xargs -0 git update-index --assume-unchanged

To undo that command:

    git ls-files .settings | tr '\n' '\0' | xargs -0 git update-index --no-assume-unchanged

## Other Relevant Maven Commands

    mvn test
    mvn clean
