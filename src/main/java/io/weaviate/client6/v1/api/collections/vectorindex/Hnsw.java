package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Hnsw(
    @SerializedName("distance") Distance distance,
    @SerializedName("ef") Integer ef,
    @SerializedName("efConstruction") Integer efConstruction,
    @SerializedName("maxConnections") Integer maxConnections,
    @SerializedName("vectorCacheMaxObjects") Long vectorCacheMaxObjects,
    @SerializedName("cleanupIntervalSeconds") Integer cleanupIntervalSeconds,
    @SerializedName("filterStrategy") FilterStrategy filterStrategy,

    @SerializedName("dynamicEfMin") Integer dynamicEfMin,
    @SerializedName("dynamicEfMax") Integer dynamicEfMax,
    @SerializedName("dynamicEfFactor") Integer dynamicEfFactor,
    @SerializedName("flatSearchCutoff") Integer flatSearchCutoff,
    @SerializedName("skip") Boolean skipVectorization) implements VectorIndex {

  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.HNSW;
  }

  @Override
  public Object config() {
    return this;
  }

  public static Hnsw of() {
    return of(ObjectBuilder.identity());
  }

  public static Hnsw of(Function<Builder, ObjectBuilder<Hnsw>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Hnsw(Builder builder) {
    this(
        builder.distance,
        builder.ef,
        builder.efConstruction,
        builder.maxConnections,
        builder.vectorCacheMaxObjects,
        builder.cleanupIntervalSeconds,
        builder.filterStrategy,
        builder.dynamicEfMin,
        builder.dynamicEfMax,
        builder.dynamicEfFactor,
        builder.flatSearchCutoff,
        builder.skipVectorization);
  }

  public static class Builder implements ObjectBuilder<Hnsw> {
    private Distance distance;
    private Integer ef;
    private Integer efConstruction;
    private Integer maxConnections;
    private Long vectorCacheMaxObjects;
    private Integer cleanupIntervalSeconds;
    private FilterStrategy filterStrategy;

    private Integer dynamicEfMin;
    private Integer dynamicEfMax;
    private Integer dynamicEfFactor;
    private Integer flatSearchCutoff;
    private Boolean skipVectorization;

    public Builder distance(Distance distance) {
      this.distance = distance;
      return this;
    }

    public Builder ef(int ef) {
      this.ef = ef;
      return this;
    }

    public final Builder efConstruction(int efConstruction) {
      this.efConstruction = efConstruction;
      return this;
    }

    public final Builder maxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    public final Builder vectorCacheMaxObjects(long vectorCacheMaxObjects) {
      this.vectorCacheMaxObjects = vectorCacheMaxObjects;
      return this;
    }

    public final Builder cleanupIntervalSeconds(int cleanupIntervalSeconds) {
      this.cleanupIntervalSeconds = cleanupIntervalSeconds;
      return this;
    }

    public final Builder filterStrategy(FilterStrategy filterStrategy) {
      this.filterStrategy = filterStrategy;
      return this;
    }

    public final Builder dynamicEfMin(int dynamicEfMin) {
      this.dynamicEfMin = dynamicEfMin;
      return this;
    }

    public final Builder dynamicEfMax(int dynamicEfMax) {
      this.dynamicEfMax = dynamicEfMax;
      return this;
    }

    public final Builder dynamicEfFactor(int dynamicEfFactor) {
      this.dynamicEfFactor = dynamicEfFactor;
      return this;
    }

    public final Builder flatSearchCutoff(int flatSearchCutoff) {
      this.flatSearchCutoff = flatSearchCutoff;
      return this;
    }

    public final Builder skipVectorization(boolean skip) {
      this.skipVectorization = skip;
      return this;
    }

    @Override
    public Hnsw build() {
      return new Hnsw(this);
    }
  }

  public enum FilterStrategy {
    @SerializedName("sweeping")
    SWEEPING,
    @SerializedName("acorn")
    ACORN;
  }
}
