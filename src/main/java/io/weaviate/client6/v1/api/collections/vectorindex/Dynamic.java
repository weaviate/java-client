package io.weaviate.client6.v1.api.collections.vectorindex;

import java.io.IOException;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Dynamic(
    @SerializedName("hnsw") Hnsw hnsw,
    @SerializedName("flat") Flat flat,
    @SerializedName("threshold") Long threshold)
    implements VectorIndex {

  @Override
  public VectorIndex.Kind _kind() {
    return VectorIndex.Kind.DYNAMIC;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Dynamic of() {
    return of(ObjectBuilder.identity());
  }

  public static Dynamic of(Function<Builder, ObjectBuilder<Dynamic>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Dynamic(Builder builder) {
    this(
        builder.hnsw,
        builder.flat,
        builder.threshold);
  }

  public static class Builder implements ObjectBuilder<Dynamic> {

    private Hnsw hnsw;
    private Flat flat;
    private Long threshold;

    public Builder hnsw(Hnsw hnsw) {
      this.hnsw = hnsw;
      return this;
    }

    public Builder flat(Flat flat) {
      this.flat = flat;
      return this;
    }

    public Builder threshold(long threshold) {
      this.threshold = threshold;
      return this;
    }

    @Override
    public Dynamic build() {
      return new Dynamic(this);
    }
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      var rawType = type.getRawType();
      if (!Dynamic.class.isAssignableFrom(rawType)) {
        return null;
      }

      final var hnswAdapter = gson.getDelegateAdapter(VectorIndex.CustomTypeAdapterFactory.INSTANCE,
          TypeToken.get(Hnsw.class));
      final var flatAdapter = gson.getDelegateAdapter(VectorIndex.CustomTypeAdapterFactory.INSTANCE,
          TypeToken.get(Flat.class));

      return (TypeAdapter<T>) new TypeAdapter<Dynamic>() {

        @Override
        public void write(JsonWriter out, Dynamic value) throws IOException {

          var dynamic = new JsonObject();

          dynamic.addProperty("threshold", value.threshold);
          dynamic.add("hnsw", hnswAdapter.toJsonTree(value.hnsw));
          dynamic.add("flat", flatAdapter.toJsonTree(value.flat));

          Streams.write(dynamic, out);
        }

        @Override
        public Dynamic read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          var hnsw = hnswAdapter.fromJsonTree(jsonObject.get("hnsw"));
          var flat = flatAdapter.fromJsonTree(jsonObject.get("flat"));
          var threshold = jsonObject.get("threshold").getAsLong();
          return new Dynamic(hnsw, flat, threshold);
        }
      }.nullSafe();
    }
  }
}
