package io.weaviate.client6.v1.api.collections.quantizers;

import io.weaviate.client6.v1.api.collections.Quantization;

public record Uncompressed() implements Quantization {

  @Override
  public Quantization.Kind _kind() {
    return Quantization.Kind.UNCOMPRESSED;
  }

  @Override
  public Object _self() {
    return true;
  }

  public static Uncompressed of() {
    return new Uncompressed();
  }
}
