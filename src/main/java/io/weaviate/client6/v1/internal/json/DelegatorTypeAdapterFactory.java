package io.weaviate.client6.v1.internal.json;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * DelegatorTypeAdapterFactory delegates de-/serialization of a type to the
 * {@link JsonDelegate} registerred via {@link DelegateJson} annotation.
 *
 * It cannot handle generic types, e.g. {@code Person<PetT>}.
 */
public class DelegatorTypeAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    var rawType = type.getRawType();
    var jsonDelegate = rawType.getAnnotation(DelegateJson.class);
    if (jsonDelegate == null) {
      return null;
    }

    @SuppressWarnings("unchecked")
    var delegateType = (Class<JsonDelegate<T>>) jsonDelegate.value();
    var delegate = gson.getDelegateAdapter(this, TypeToken.get(delegateType));

    return new TypeAdapter<T>() {

      @Override
      public T read(JsonReader in) throws IOException {
        var dto = (JsonDelegate<T>) delegate.read(in);
        return dto.toModel();
      }

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        try {
          var constructor = delegateType.getDeclaredConstructor(rawType);
          constructor.setAccessible(true);
          var dto = constructor.newInstance(value);
          delegate.write(out, dto);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
