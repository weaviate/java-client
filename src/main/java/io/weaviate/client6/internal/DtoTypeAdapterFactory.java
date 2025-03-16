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
 * The purpose of the DtoTypeAdapterFactory is to de-/serialize objects using
 * their DTOs.
 */
public class DtoTypeAdapterFactory implements TypeAdapterFactory {
  private static final Map<Class<?>, DtoThing<?>> registry = new HashMap<>();

  public static <T> void register(Class<T> model, Class<?> dto, GsonSerializable<T, DTO<T>> convert) {
    registry.putIfAbsent(model, new DtoThing<T>(model, (Class<DTO<T>>) dto, convert));
  }

  record DtoThing<T>(Class<T> model, Class<DTO<T>> dto, GsonSerializable<T, DTO<T>> convert) {
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<?> cls = type.getRawType();
    if (!registry.containsKey(cls)) {
      return null;
    }
    DtoThing<T> entry = (DtoThing<T>) registry.get(cls);
    TypeAdapter<DTO<T>> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.dto));

    return new TypeAdapter<T>() {

      @Override
      public T read(JsonReader in) throws IOException {
        DTO<T> dto = delegate.read(in);
        return dto.toModel();
      }

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        var dto = entry.convert.toDTO(value);
        delegate.write(out, dto);
      }
    };
  }

  public interface DTO<M> {
    M toModel();
  }

  @FunctionalInterface
  public interface GsonSerializable<M, D extends DTO<?>> {
    D toDTO(M model);
  }
}
