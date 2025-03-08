package io.weaviate.client6.v1.data;

import java.util.Map;
import java.util.function.Consumer;

public class CustomMetadata {
  String id;
  Vectors vectors;

  public CustomMetadata id(String id) {
    this.id = id;
    return this;
  }

  public CustomMetadata vectors(Vectors vectors) {
    this.vectors = vectors;
    return this;
  }

  public CustomMetadata vectors(Map<String, ? extends Object> vectors) {
    this.vectors = new Vectors(vectors);
    return this;
  }

  CustomMetadata(Consumer<CustomMetadata> options) {
    options.accept(this);
  }
}
