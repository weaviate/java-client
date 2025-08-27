# Weaviate Java client  <img alt='Weaviate logo' src='https://raw.githubusercontent.com/weaviate/weaviate/19de0956c69b66c5552447e84d016f4fe29d12c9/docs/assets/weaviate-logo.png' width='180' align='right' />

[![Build Status](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml/badge.svg?branch=main)](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml)

Official Weaviate Java Client.

## Usage

To start using Weaviate Java Client add the dependency to `pom.xml`:

```xml
<dependency>
    <groupId>io.weaviate</groupId>
    <artifactId>client6</artifactId>
    <version>6.0.0-beta4</version>
</dependency>
```

### Uber JAR🫙

If you're building an uber-JAR with something like `maven-assembly-plugin`, use a shaded version with classifier `all`.
This ensures that all dynamically-loaded dependecies of `io.grpc` are resolved correctly.

```xml
<dependency>
    <groupId>io.weaviate</groupId>
    <artifactId>client6</artifactId>
    <version>6.0.0-beta4</version>
    <classifier>all</classifier>
</dependency>
```

### SNAPSHOT releases

The latest development version of `client6` is released after every merged pull request. To include it in you project set the version to `6.0.0-SNAPSHOT` and [configure your `<repositories>` section accordingly](https://central.sonatype.org/publish/publish-portal-snapshots/#consuming-snapshot-releases-for-your-project).
Please be mindful of the fact that this is not a stable release and breaking changes may be introduced.

Snapshot releases overwrite each other, so no two releases are alike. If you find a bug in one of the `SNAPSHOT` versions that you'd like to report, please include the output of `Debug.printBuildInfo()` in the ticket's description.

```java
import io.weaviate.client6.v1.internal.Debug;

public class App {
    public static void main(String[] args) {
        Debug.printBuildInfo();

        // ...the rest of your application code...
    }
}
```

### Gson and reflective access to internal JDK classes

The client uses Google's [`gson`](https://github.com/google/gson) for JSON de-/serialization which does reflection on internal `java.lang` classes. This is _not allowed by default_ in Java 9 and above.

To work around this, it's necessary to add this JVM command line argument:

```
--add-opens=java.base/java.lang=ALL-UNNAMED
```

If you're using Gradle, you can add this instead to your `application` block in your `build.gradle.kts` file:

```kotlin
applicationDefaultJvmArgs += listOf(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
)
```

## Useful resources

- [Documentation](https://weaviate.io/developers/weaviate/current/client-libraries/java.html).
- [StackOverflow for questions about Weaviate](https://stackoverflow.com/questions/tagged/weaviate).
- [Github for issues in client6](https://github.com/weaviate/java-client/issues).

## Contributing

- [How to Contribute](https://github.com/weaviate/java-client/blob/main/CONTRIBUTE.md).

