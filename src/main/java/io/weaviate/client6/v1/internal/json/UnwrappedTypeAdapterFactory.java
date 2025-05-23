package io.weaviate.client6.v1.internal.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class UnwrappedTypeAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    var rawType = typeToken.getRawType();

    // Let other TypeAdapters handle the Unwrapped objects, we only want to handle
    // to outer ones
    if (rawType.isPrimitive() || rawType.isArray() || rawType.isEnum() || Unwrapped.class.isAssignableFrom(rawType)) {
      return null;
    }

    // Check if any of the properties implements Unwrapped
    Map<String, TypeAdapter<? extends Object>> adapters = new HashMap<>();
    Class<?> tmp_unwrappable = null;
    String tmp_unwrappable_name = null;
    Set<String> outerFields = new HashSet<>();
    for (var field : rawType.getDeclaredFields()) {
      if (Unwrapped.class.isAssignableFrom(field.getType())) {
        var fieldDelegate = gson.getDelegateAdapter(this, TypeToken.get(field.getType()));
        adapters.put(field.getName(), fieldDelegate);
        outerFields.add(field.getName());

        tmp_unwrappable = field.getType();
        tmp_unwrappable_name = field.getName();
        break;
      }
    }
    Class<?> unwrappable = tmp_unwrappable;
    String unwrappableName = tmp_unwrappable_name;

    // No custom serialization for this type.
    if (adapters.isEmpty()) {
      return null;
    }

    // final TypeAdapter<JsonElement> elementAdapter = gson.getDelegateAdapter(this,
    // TypeToken.get(JsonElement.class));
    final var delegate = gson.getDelegateAdapter(this, typeToken);
    return new TypeAdapter<T>() {

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        // Start with JSON tree representation of the object we want to write
        var tree = delegate.toJsonTree(value);
        var object = tree.getAsJsonObject();

        out.beginObject();
        // Then check all fields that may need to be unwrapped
        for (var fieldName : adapters.keySet()) {
          try {
            var field = value.getClass().getDeclaredField(fieldName);

            field.setAccessible(true);
            var fieldValue = field.get(value);

            @SuppressWarnings("unchecked") // let's see if this works
            var serializer = (TypeAdapter<Object>) adapters.get(fieldName);
            var fieldElement = serializer.toJsonTree(fieldValue);

            if (((Unwrapped) fieldValue).shouldUnwrap()) {
              // Write every property of the nested element to parent
              var fieldObject = fieldElement.getAsJsonObject();
              for (var entry : fieldObject.entrySet()) {
                out.name(entry.getKey());
                Streams.write(entry.getValue(), out);
              }
            } else {
              // Write the nested element
              out.name(fieldName);
              Streams.write(fieldElement, out);
            }

            // Exclude that from the object to avoid overwriting it
            // I guess we can remove both this and the else-branch
            object.remove(fieldName);
          } catch (NoSuchFieldException e) {
            // Should not happen
            System.out.println(e);
          } catch (IllegalAccessException e) {
            // Should not happen either
            System.out.println(e);
          }
        }

        // Write the remaining properties
        for (final var entry : object.entrySet()) {
          out.name(entry.getKey());
          Streams.write(entry.getValue(), out);
        }
        out.endObject();
      }

      @Override
      public T read(JsonReader in) throws IOException {
        // TODO: make sure to setIgnoreUnknownProperties(true) on the builder.
        var object = JsonParser.parseReader(in).getAsJsonObject();

        // Read outer object itlself.
        T result = delegate.fromJsonTree(object);

        if (object.keySet().contains(unwrappableName)) {
          // We've already parsed everything there was to parse.
          return result;
        }

        try {
          var inner = adapters.get(unwrappableName).fromJsonTree(object);

          rawType.getDeclaredField(unwrappableName).setAccessible(true);
          rawType.getDeclaredField(unwrappableName).set(result, inner);
        } catch (Exception e) {
          System.out.println(e);
        }

        return result;
      }
    };
  }
}
