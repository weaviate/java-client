# Weaviate Java client  <img alt='Weaviate logo' src='https://raw.githubusercontent.com/semi-technologies/weaviate/19de0956c69b66c5552447e84d016f4fe29d12c9/docs/assets/weaviate-logo.png' width='180' align='right' />

A Java native client for weaviate.

## Usage

In order to get start using the java client one needs to add it's dependency:

```xml
<dependency>
  <groupId>technology.semi.weaviate</groupId>
  <artifactId>client</artifactId>
  <version>3.5.0</version>
</dependency>
```

Here's a simple code to start up working with Java client:

1. Add dependency to your java project.

2. Connect to Weaviate on `localhost:8080` and fetch meta information

```java
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.misc.model.Meta;

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
- [Github for issues](https://github.com/semi-technologies/weaviate-java-client/issues).

## Contributing

- [How to Contribute](https://github.com/semi-technologies/weaviate-java-client/blob/master/CONTRIBUTE.md).

## Build Status

| Branch   | Status        |
| -------- |:-------------:|
| Main     | [![Build Status](https://travis-ci.com/semi-technologies/weaviate-java-client.svg?token=YPa1Pbr1QfTDsrnqbHLf&branch=main)](https://travis-ci.com/github/semi-technologies/weaviate-java-client)
