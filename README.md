# Weaviate Java client  <img alt='Weaviate logo' src='https://raw.githubusercontent.com/weaviate/weaviate/19de0956c69b66c5552447e84d016f4fe29d12c9/docs/assets/weaviate-logo.png' width='180' align='right' />

A Java native client for weaviate.

## Usage

In order to get start using the java client one needs to add it's dependency:

```xml
<dependency>
  <groupId>io.weaviate</groupId>
  <artifactId>client</artifactId>
  <version>4.1.0</version>
</dependency>
```


### For applications on Java 9 or above

The client utilizes Gson for JSON serialization/deserialization and Gson uses reflection of internal `java.lang` classes
to do it. This is not allowed by default in Java 9 and above.

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


Here's a simple code to start up working with Java client:

1. Add dependency to your java project.

2. Connect to Weaviate on `localhost:8080` and fetch meta information

```java
package io.weaviate;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;

public class App {
  public static void main(String[] args) {
    Config config = new Config("http", "localhost:8080");
    WeaviateClient client = new WeaviateClient(config);
    Result<Meta> meta = client.misc().metaGetter().run();
    if (meta.hasErrors()) {
      System.out.printf("Error: %s\n", meta.getError().getMessages());
    } else {
      System.out.printf("meta.hostname: %s\n", meta.getResult().getHostname());
      System.out.printf("meta.version: %s\n", meta.getResult().getVersion());
      System.out.printf("meta.modules: %s\n", meta.getResult().getModules());
    }
  }
}
```

## Documentation

- [Documentation](https://weaviate.io/developers/weaviate/current/client-libraries/java.html).

## Support

- [Stackoverflow for questions](https://stackoverflow.com/questions/tagged/weaviate).
- [Github for issues](https://github.com/weaviate/java-client/issues).

## Contributing

- [How to Contribute](https://github.com/weaviate/java-client/blob/main/CONTRIBUTE.md).

## Build Status

[![Build Status](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml/badge.svg?branch=main)](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml)
