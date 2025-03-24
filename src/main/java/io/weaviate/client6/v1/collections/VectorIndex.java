package io.weaviate.client6.v1.collections;

import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

public record VectorIndex<V extends Vectorizer>(
    @SerializedName("vectorIndexType") IndexType type,
    @SerializedName("vectorizer") V vectorizer,
    @SerializedName("vectorIndexConfig") IndexingStrategy configuration) {

  public enum IndexType {
    @SerializedName("hnsw")
    HNSW;
  }

  public VectorIndex(IndexingStrategy index, V vectorizer) {
    this(index.type(), vectorizer, index);
  }

  public VectorIndex(V vectorizer) {
    this(IndexingStrategy.hnsw(), vectorizer);
  }

  public static sealed interface IndexingStrategy permits HNSW {
    IndexType type();

    public static IndexingStrategy hnsw() {
      return new HNSW();
    }

    public static IndexingStrategy hnsw(Consumer<HNSW.Options> options) {
      return HNSW.with(options);
    }
  }
}
