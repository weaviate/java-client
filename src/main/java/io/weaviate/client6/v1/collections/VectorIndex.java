package io.weaviate.client6.v1.collections;

import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

public record VectorIndex<V extends Vectorizer>(
    @SerializedName("vectorIndexType") IndexType type,
    @SerializedName("vectorizer") V vectorizer,
    @SerializedName("vectorIndexConfig") Configuration configuration) {

  public enum IndexType {
    @SerializedName("hnsw")
    HNSW;
  }

  public static sealed interface Configuration permits HNSW {
  }

  private VectorIndex(IndexType type, V vectorizer) {
    this(type, vectorizer, null);
  }

  public static VectorIndex<NoneVectorizer> bare() {
    return new VectorIndex<>(null, Vectorizer.none());
  }

  public static VectorIndex<NoneVectorizer> hnsw() {
    return new VectorIndex<>(IndexType.HNSW, Vectorizer.none());
  }

  public static <V extends Vectorizer> VectorIndex<V> hnsw(V vectorizer) {
    return new VectorIndex<>(IndexType.HNSW, vectorizer);
  }

  public static <V extends Vectorizer> VectorIndex<V> hnsw(V vectorizer, Consumer<HNSW.Options> options) {
    return new VectorIndex<>(IndexType.HNSW, vectorizer, HNSW.with(options));
  }
}
