package io.weaviate.client6.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * DtoTypeAdapterFactory de-/serializes objects using their registerred DTOs.
 *
 * <p>
 * DTO classes must implement {@link Dto}, which produces the original model.
 * Meanwhile, models do not need to be modified, to avoid leaking
 * de-/serialization details.
 *
 * <p>
 * Usage:
 *
 * <pre>{@code
 * public class HttpHanlder {
 *   static {
 *     DtoTypeAdapterFactory.register(
 *         MyDomainObject.class,
 *         MyDtoObject.class,
 *         domain -> new MyDtoObject(domain));
 *   }
 *   static final Gson gson = new GsonBuilder()
 *       .registerTypeAdapterFactory(new DtoTypeAdapterFactory())
 *       .create();
 * }
 * }</pre>
 */
public class DtoTypeAdapterFactory implements TypeAdapterFactory {
  private static boolean locked = false;
  private static final Map<Class<?>, Pair<?, ?>> registry = new HashMap<>();

  /**
   * Register a model-DTO pair.
   *
   * <p>
   * Only one DTO can be registerred per model.
   * Subsequent registrations will be ignored.
   */
  public static <M, DTO extends Dto<M>> void register(Class<M> model, Class<DTO> dto,
      ModelConverter<M, Dto<M>> convert) {
    registry.putIfAbsent(model, new Pair<M, DTO>(dto, convert));
  }

  /**
   * Get model-DTO pair for the provided model class. Returns null if no pair is
   * registerred. In this case {@link #create} should also return null.
   *
   * <p>
   * Conversion to {@code Pair<M, DTO>} is safe, as entries to {@link #registry}
   * can only be added via {@link #register}, which is type-safe.
   */
  @SuppressWarnings("unchecked")
  private static <M, DTO extends Dto<M>> Pair<M, DTO> getPair(TypeToken<? super M> type) {
    var cls = type.getRawType();
    if (!registry.containsKey(cls)) {
      return null;
    }
    return (Pair<M, DTO>) registry.get(cls);
  }

  /** Dto produces a domain model. */
  public interface Dto<M> {
    M toModel();
  }

  /** ModelConverter converts domain model to a DTO. */
  @FunctionalInterface
  public interface ModelConverter<M, D extends Dto<?>> {
    D toDTO(M model);
  }

  record Pair<M, DTO extends Dto<M>>(Class<DTO> dto, ModelConverter<M, Dto<M>> convert) {
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    var pair = getPair(type);
    if (pair == null) {
      return null;
    }
    var delegate = gson.getDelegateAdapter(this, TypeToken.get(pair.dto));
    return new TypeAdapter<T>() {

      @Override
      public T read(JsonReader in) throws IOException {
        var dto = delegate.read(in);
        return dto.toModel();
      }

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        var dto = pair.convert.toDTO(value);
        delegate.write(out, dto);
      }
    };
  }
}
