package io.weaviate.client6.v1.api.collections;

import java.util.UUID;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record ObjectMetadata(
    @SerializedName("id") String uuid,
    @SerializedName("vectors") Vectors vectors,
    @SerializedName("creationTimeUnix") Long createdAt,
    @SerializedName("lastUpdateTImeUnix") Long lastUpdatedAt) implements WeaviateMetadata {

  public ObjectMetadata(Builder builder) {
    this(builder.uuid, builder.vectors, null, null);
  }

  public static ObjectMetadata of() {
    return of(ObjectBuilder.identity());
  }

  public static ObjectMetadata of(Function<Builder, ObjectBuilder<ObjectMetadata>> fn) {
    return fn.apply(new Builder()).build();
  }

  public static class Builder implements ObjectBuilder<ObjectMetadata> {
    private String uuid = UUID.randomUUID().toString();
    private Vectors vectors;

    /** Assign a custom UUID for the object. */
    public Builder uuid(UUID uuid) {
      return uuid(uuid.toString());
    }

    /** Assign a custom UUID for the object. */
    public Builder uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    /** Attach custom vectors to the object.. */
    public Builder vectors(Vectors... vectors) {
      this.vectors = new Vectors(vectors);
      return this;
    }

    @Override
    public ObjectMetadata build() {
      return new ObjectMetadata(this);
    }
  }
}
