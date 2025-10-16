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

import io.weaviate.client6.v1.api.collections.Encoding;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record MultiVector(
    @SerializedName("enabled") boolean enabled,
    @SerializedName("aggregation") Aggregation aggregation,
    Encoding encoding) {

  public enum Aggregation {
    @SerializedName("MAX_SIM")
    MAX_SIM;
  }

  public static MultiVector of() {
    return of(ObjectBuilder.identity());
  }

  public static MultiVector of(Function<Builder, ObjectBuilder<MultiVector>> fn) {
    return fn.apply(new Builder()).build();
  }

  public MultiVector(Builder builder) {
    this(
        builder.enabled,
        builder.aggregation,
        builder.encoding);
  }

  public static class Builder implements ObjectBuilder<MultiVector> {
    private boolean enabled = true;
    private Aggregation aggregation;
    private Encoding encoding;

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder aggregation(Aggregation aggregation) {
      this.aggregation = aggregation;
      return this;
    }

    public Builder encoding(Encoding encoding) {
      this.encoding = encoding;
      return this;
    }

    @Override
    public MultiVector build() {
      return new MultiVector(this);
    }
  }

  public enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final var rawType = type.getRawType();
      if (!MultiVector.class.isAssignableFrom(rawType)) {
        return null;
      }

      final TypeAdapter<MultiVector> adapter = (TypeAdapter<MultiVector>) gson.getDelegateAdapter(this, type);

      return (TypeAdapter<T>) new TypeAdapter<MultiVector>() {

        @Override
        public void write(JsonWriter out, MultiVector value) throws IOException {
          var config = adapter.toJsonTree(value);

          if (value.encoding() != null) {
            // Replace { "encoding": {...}} with { "muvera": {...}}
            // where "muvera" is the kind of encoding configured.
            config.getAsJsonObject()
                .add(value.encoding()._kind().jsonValue(),
                    config.getAsJsonObject().remove("encoding"));
          }

          Streams.write(config, out);
        }

        @Override
        public MultiVector read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          String encodingKind = null;
          for (var kind : Encoding.Kind.values()) {
            if (jsonObject.has(kind.jsonValue())) {
              encodingKind = kind.jsonValue();
              break;
            }
          }

          if (encodingKind != null) {
            JsonObject encoding = new JsonObject();
            encoding.add(encodingKind, jsonObject.remove(encodingKind));
            jsonObject.add("encoding", encoding);
          }

          return adapter.fromJsonTree(jsonObject);
        }
      }.nullSafe();
    }
  }

}
