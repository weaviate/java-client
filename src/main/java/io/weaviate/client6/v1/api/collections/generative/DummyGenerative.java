package io.weaviate.client6.v1.api.collections.generative;

import io.weaviate.client6.v1.api.collections.Generative;

public record DummyGenerative() implements Generative {
  @Override
  public Kind _kind() {
    return Generative.Kind.DUMMY;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static record Metadata() implements ProviderMetadata {
  }
}
