# Introduction

This workspace can be built using Gradle..

The Gradle build will include all bnd projects (1) and all other projects that
have a build.gradle file.

The Gradle build templates are placed in cnf/gradle.

(1) A directory directly below the workspace root directory, with a bnd.bnd file.


# First Time Configuration

You can install Gradle in your workspace so that the workspace can be built on
systems that do not have Gradle installed. This installation must be performed
on a system that does have Gradle installed. The procedure is:
* Open a shell
* Go to the workspace root directory
* Run (assuming Gradle is in the search path of the shell):

```
gradle wrapper
gradle tasks
```

* In your version control system ignore the following file and directories relative to the workspace root directory (in Git ignore format):

```
.gradle/
/generated/
/reports/
```

* Commit to your version control system the following files and directories relative to the workspace root directory (in Git ignore format):

```
/.gradle-wrapper/
/build.gradle
/cnf/build.gradle
/cnf/gradle/
/gradlew
/gradlew.bat
/settings.gradle
```
