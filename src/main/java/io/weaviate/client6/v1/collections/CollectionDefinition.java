package io.weaviate.client6.v1.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CollectionDefinition {
  public final String name;
  public final List<Property> properties;
  public final Map<String, VectorConfig> vectorConfig;

  public CollectionDefinition(String name, Consumer<Configuration> options) {
    var config = new Configuration(options);

    this.name = name;
    this.properties = config.properties;
    this.vectorConfig = config.vectorConfig;
  }

  public interface VectorConfig {
    Object vectorizer();

    String indexType();

    Object indexConfiguration();
  }

  // Tucked Builder for additional collection configuration.
  public static class Configuration {
    public List<Property> properties;
    public final Map<String, VectorConfig> vectorConfig;

    public Configuration properties(Property... properties) {
      this.properties = Arrays.asList(properties);
      return this;
    }

    // By default, we configure a "none" vectorizer.
    public Configuration vector(String name) {
      this.vectorConfig.put(name, new NoneVectorIndex());
      return this;
    }

    Configuration(Consumer<Configuration> options) {
      this.properties = new ArrayList<>();
      this.vectorConfig = new HashMap<>();
      options.accept(this);
    }
  }

  // JSON serialization ---------------
  static CollectionDefinition fromJson(Gson gson, InputStream input) throws IOException {
    try (var r = new InputStreamReader(input)) {
      var dto = gson.fromJson(r, CollectionDefinitionDTO.class);
      return dto.toCollectionDefinition();
    }
  }

  public String toJson(Gson gson) {
    return gson.toJson(new CollectionDefinitionDTO(this));
  }
}
