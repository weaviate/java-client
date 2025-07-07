# Weaviate Java client  <img alt='Weaviate logo' src='https://raw.githubusercontent.com/weaviate/weaviate/19de0956c69b66c5552447e84d016f4fe29d12c9/docs/assets/weaviate-logo.png' width='180' align='right' />

[![Build Status](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml/badge.svg?branch=main)](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml)

Official Weaviate Java Client.

## Usage

To start using Weaviate Java Client add this dependency to `pom.xml`:

```xml

<dependency>
    <groupId>io.weaviate</groupId>
    <artifactId>client6</artifactId>
    <version>6.0.0-beta2</version>
</dependency>
```

### For applications on Java 9 or above

The client uses Google's [`gson`](https://github.com/google/gson) for JSON de-/serialization which does reflection on internal `java.lang` classes. This is _not allowed by default_ in Java 9 and above.

To work around this, it's necessary to add this JVM commandline argument:

```
--add-opens=java.base/java.lang=ALL-UNNAMED
```

If you're using Gradle, you can add this instead to your `application` block in your `build.gradle.kts` file:

```kotlin
applicationDefaultJvmArgs += listOf(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
)
```

## Documentation

- [Documentation](https://weaviate.io/developers/weaviate/current/client-libraries/java.html).

## Support

- [Stackoverflow for questions](https://stackoverflow.com/questions/tagged/weaviate).
- [Github for issues](https://github.com/weaviate/java-client/issues).

## Contributing

- [How to Contribute](https://github.com/weaviate/java-client/blob/main/CONTRIBUTE.md).

