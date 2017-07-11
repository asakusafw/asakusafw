# Gradle Launcher

This artifact provides an easy way of Gradle access to your program.

## Artifact

### Maven project

```xml
<dependencies>
  <dependency>
    <groupId>com.asakusafw</groupId>
    <artifactId>gradle-launcher</artifactId>
    <version>x.y.z</version>
  </dependency>
  <!-- require a slf4j-api implementaton -->
  <dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>x.y.z</version>
  </dependency>
</dependencies>
```

### Gradle project

```gradle
repositories {
    mavenCentral()
    maven { url 'http://asakusafw.s3.amazonaws.com/maven/releases' }
    maven { url 'http://asakusafw.s3.amazonaws.com/maven/snapshots' }
    maven { url 'https://repo.gradle.org/gradle/libs-releases' }
}

dependencies {
    compile "com.asakusafw:gradle-launcher:x.y.z"
    runtime 'org.slf4j:slf4j-simple:x.y.z'
}
```

## Example

```java
// connect to project on /path/to/project
BasicProject project = new BasicProject(Paths.get("/path/to/project"))
        .with(EnvironmentConfigurator.system()) // inherit system environment variables
        .with(PropertyConfigurator.system())    // inherit system properties
        .with(ContentsConfigurator.copy("/path/to/template-dir")); // merge template files

// run gradle tasks
project.gradle("clean", "build", "assemble");

// obtain a generated files
Bundle contents = project.getContents();
Path assembly = contents.get("build/libs/project.jar");
```
