package io.weaviate.client6.v1.api.collections.vectorindex;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public class Hnsw extends BaseVectorIndex {
  @SerializedName("distance")
  private final Distance distance;
  @SerializedName("ef")
  private final Integer ef;
  @SerializedName("efConstruction")
  private final Integer efConstruction;
  @SerializedName("maxConnections")
  private final Integer maxConnections;
  @SerializedName("vectorCacheMaxObjects")
  private final Long vectorCacheMaxObjects;
  @SerializedName("cleanupIntervalSeconds")
  private final Integer cleanupIntervalSeconds;
  @SerializedName("filterStrategy")
  private final FilterStrategy filterStrategy;

  @SerializedName("dynamicEfMin")
  private final Integer dynamicEfMin;
  @SerializedName("dynamicEfMax")
  private final Integer dynamicEfMax;
  @SerializedName("dynamicEfFactor")
  private final Integer dynamicEfFactor;
  @SerializedName("flatSearchCutoff")
  private final Integer flatSearchCutoff;
  @SerializedName("skip")
  Boolean skipVectorization;

  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.HNSW;
  }

  @Override
  public Object config() {
    return this;
  }

  @Override
  public Vectorizer vectorizer() {
    return this.vectorizer;
  }

  public static Hnsw of(Vectorizer vectorizer) {
    return of(vectorizer, ObjectBuilder.identity());
  }

  public static Hnsw of(Vectorizer vectorizer, Function<Builder, ObjectBuilder<Hnsw>> fn) {
    return fn.apply(new Builder(vectorizer)).build();
  }

  public Hnsw(Builder builder) {
    super(builder.vectorizer);
    this.distance = builder.distance;
    this.ef = builder.ef;
    this.efConstruction = builder.efConstruction;
    this.maxConnections = builder.maxConnections;
    this.vectorCacheMaxObjects = builder.vectorCacheMaxObjects;
    this.cleanupIntervalSeconds = builder.cleanupIntervalSeconds;
    this.filterStrategy = builder.filterStrategy;
    this.dynamicEfMin = builder.dynamicEfMin;
    this.dynamicEfMax = builder.dynamicEfMax;
    this.dynamicEfFactor = builder.dynamicEfFactor;
    this.flatSearchCutoff = builder.flatSearchCutoff;
    this.skipVectorization = builder.skipVectorization;
  }

  public static class Builder implements ObjectBuilder<Hnsw> {
    // Required parameters.
    private final Vectorizer vectorizer;

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

    public Builder(Vectorizer vectorizer) {
      this.vectorizer = vectorizer;
    }

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
