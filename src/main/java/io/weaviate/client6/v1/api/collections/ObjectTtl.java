package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.query.BaseQueryOptions;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record ObjectTtl(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("defaultTtl") Integer defaultTtlSeconds,
    @SerializedName("deleteOn") String deleteOn,
    @SerializedName("filterExpiredObjects") Boolean filterExpiredObjects) {

  public static ObjectTtl of(Function<Builder, ObjectBuilder<ObjectTtl>> fn) {
    return fn.apply(new Builder()).build();
  }

  public ObjectTtl(Builder builder) {
    this(
        builder.enabled,
        builder.defaultTtlSeconds,
        builder.deleteOn,
        builder.filterExpiredObjects);
  }

  public static class Builder implements ObjectBuilder<ObjectTtl> {
    private boolean enabled = true;
    private Integer defaultTtlSeconds;
    private String deleteOn;
    private Boolean filterExpiredObjects;

    /** Enable / disable object TTL for this collection. */
    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /** Default TTL for all objects in this collection. */
    public Builder defaultTtlSeconds(int seconds) {
      this.defaultTtlSeconds = seconds;
      return this;
    }

    /**
     * If enabled, excludes expired objects from search results.
     * Expired objects may be temporarily present until the next deletion cycle.
     */
    public Builder filterExpiredObjects(boolean enabled) {
      this.filterExpiredObjects = enabled;
      return this;
    }

    /**
     * Measure TTL relative an arbitrary {@link DataType#DATE}
     * property on the object.
     */
    public Builder deleteByDateProperty(String property) {
      this.deleteOn = property;
      return this;
    }

    /** Measure TTL relative to objects' creation time. */
    public Builder deleteByCreationTime() {
      this.deleteOn = BaseQueryOptions.CREATION_TIME_PROPERTY;
      return this;
    }

    /** Measure TTL relative to objects' last update time. */
    public Builder deleteByUpdateTime() {
      this.deleteOn = BaseQueryOptions.LAST_UPDATE_TIME_PROPERTY;
      return this;
    }

    @Override
    public ObjectTtl build() {
      return new ObjectTtl(this);
    }
  }
}
