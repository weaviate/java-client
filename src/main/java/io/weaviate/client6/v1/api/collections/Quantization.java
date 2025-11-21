package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.quantizers.BQ;
import io.weaviate.client6.v1.api.collections.quantizers.PQ;
import io.weaviate.client6.v1.api.collections.quantizers.RQ;
import io.weaviate.client6.v1.api.collections.quantizers.SQ;
import io.weaviate.client6.v1.api.collections.quantizers.Uncompressed;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TaggedUnion;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Quantization extends TaggedUnion<Quantization.Kind, Object> {

  public enum Kind implements JsonEnum<Kind> {
    UNCOMPRESSED("skipDefaultQuantization"),
    RQ("rq"),
    BQ("bq"),
    PQ("pq"),
    SQ("sq");

    private static final Map<String, Kind> jsonValueMap = JsonEnum.collectNames(Kind.values());
    private final String jsonValue;

    private Kind(String jsonValue) {
      this.jsonValue = jsonValue;
    }

    @Override
    public String jsonValue() {
      return this.jsonValue;
    }

    public static Kind valueOfJson(String jsonValue) {
      return JsonEnum.valueOfJson(jsonValue, jsonValueMap, Kind.class);
    }
  }

  Kind _kind();

  Object _self();

  /** Disable any quantization for this collection. */
  public static Quantization uncompressed() {
    return Uncompressed.of();
  }

  /** Enable binary quantization for this collection. */
  public static Quantization bq() {
    return BQ.of();
  }

  /**
   * Enable binary quantization for this collection.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Quantization bq(Function<BQ.Builder, ObjectBuilder<BQ>> fn) {
    return BQ.of(fn);
  }

  /** Enable product quantization for this collection. */
  public static Quantization pq() {
    return PQ.of();
  }

  /**
   * Enable product quantization for this collection.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Quantization pq(Function<PQ.Builder, ObjectBuilder<PQ>> fn) {
    return PQ.of(fn);
  }

  /** Enable scalar quantization for this collection. */
  public static Quantization sq() {
    return SQ.of();
  }

  /**
   * Enable scalar quantization for this collection.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Quantization sq(Function<SQ.Builder, ObjectBuilder<SQ>> fn) {
    return SQ.of(fn);
  }

  /** Enable rotational quantization for this collection. */
  public static Quantization rq() {
    return RQ.of();
  }

  /**
   * Enable rotational quantization for this collection.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Quantization rq(Function<RQ.Builder, ObjectBuilder<RQ>> fn) {
    return RQ.of(fn);
  }

  default BQ asBQ() {
    return _as(Quantization.Kind.BQ);
  }

  default RQ asRQ() {
    return _as(Quantization.Kind.RQ);
  }

  default PQ asPQ() {
    return _as(Quantization.Kind.PQ);
  }

  default SQ asSQ() {
    return _as(Quantization.Kind.SQ);
  }

  default Uncompressed asUncompressed() {
    return _as(Quantization.Kind.UNCOMPRESSED);
  }

  default boolean isBQ() {
    return _is(Quantization.Kind.BQ);
  }

  default boolean isRQ() {
    return _is(Quantization.Kind.RQ);
  }

  default boolean isPQ() {
    return _is(Quantization.Kind.PQ);
  }

  default boolean isSQ() {
    return _is(Quantization.Kind.SQ);
  }

  default boolean isUncompressed() {
    return _is(Quantization.Kind.UNCOMPRESSED);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Quantization.Kind, TypeAdapter<? extends Quantization>> delegateAdapters = new EnumMap<>(
        Quantization.Kind.class);

    private final void addAdapter(Gson gson, Quantization.Kind kind, Class<? extends Quantization> cls) {
      delegateAdapters.put(kind,
          (TypeAdapter<? extends Quantization>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Quantization.Kind.UNCOMPRESSED, Uncompressed.class);
      addAdapter(gson, Quantization.Kind.BQ, BQ.class);
      addAdapter(gson, Quantization.Kind.RQ, RQ.class);
      addAdapter(gson, Quantization.Kind.SQ, SQ.class);
      addAdapter(gson, Quantization.Kind.PQ, PQ.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final var rawType = type.getRawType();
      if (!Quantization.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (delegateAdapters.isEmpty()) {
        init(gson);
      }

      return (TypeAdapter<T>) new TypeAdapter<Quantization>() {

        @Override
        public void write(JsonWriter out, Quantization value) throws IOException {
          if (value._kind() == Quantization.Kind.UNCOMPRESSED) {
            out.value(true);
            return;
          }
          TypeAdapter<T> adapter = (TypeAdapter<T>) delegateAdapters.get(value._kind());
          adapter.write(out, (T) value._self());
        }

        @Override
        public Quantization read(JsonReader in) throws IOException {
          var quantizerObject = JsonParser.parseReader(in).getAsJsonObject();
          var quantizationName = quantizerObject.keySet().iterator().next();
          Quantization.Kind kind;
          try {
            kind = Quantization.Kind.valueOfJson(quantizationName);
          } catch (IllegalArgumentException e) {
            return null;
          }

          if (kind == Quantization.Kind.UNCOMPRESSED) {
            return new Uncompressed();
          }

          var adapter = delegateAdapters.get(kind);
          var concreteQuantizer = quantizerObject.get(quantizationName).getAsJsonObject();
          return adapter.fromJsonTree(concreteQuantizer);
        }
      }.nullSafe();
    }
  }
}
