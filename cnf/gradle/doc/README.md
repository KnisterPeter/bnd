# Gradle Build Setup

The SVG file shows the build setup, like shown for the Java Plugin in the
Gradle User Guide.

The arrows show execution flow, so the dependencies are the other way around.

The dotted blue arrow shows a convenience flow/dependency; one can now just
run ```./gradlew jar``` in the root of the workspace and every project will be
built - bnd projects as well as java projects. Also, it avoids having to setup
even more dependencies.

The red arrows show flows/dependencies on dependent projects. For example: the
compileJava phase of a project is dependent on the bundle phase of another
project if the latter project is on the build path of the former project.
