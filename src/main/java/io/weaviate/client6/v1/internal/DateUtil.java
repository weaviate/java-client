package io.weaviate.client6.v1.internal;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class DateUtil {
  /** Prevent public initialization. */
  private DateUtil() {
  }

  /** Convert ISO8601-formatted time string to {@Olink OffsetDateTime}. */
  public static OffsetDateTime fromISO8601(String iso8601) {
    return OffsetDateTime.parse(iso8601);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() != OffsetDateTime.class) {
        return null;
      }

      return (TypeAdapter<T>) new TypeAdapter<OffsetDateTime>() {

        @Override
        public void write(JsonWriter out, OffsetDateTime value) throws IOException {
          out.value(value.toString());
        }

        @Override
        public OffsetDateTime read(JsonReader in) throws IOException {
          return OffsetDateTime.parse(in.nextString());
        }

      }.nullSafe();
    }
  }
}
