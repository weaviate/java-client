package io.weaviate.client6.v1.internal;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class DateUtil {
  private static final DateTimeFormatter RFC3339 = new DateTimeFormatterBuilder()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral('T')
      .appendValue(ChronoField.HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
      .appendLiteral(':')
      .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .appendOffsetId()
      .toFormatter();

  /** Prevent public initialization. */
  private DateUtil() {
  }

  /** Convert ISO8601-formatted time string to {@link OffsetDateTime}. */
  public static OffsetDateTime fromISO8601(String iso8601) {
    return OffsetDateTime.parse(iso8601);
  }

  public static String toRFC3339(OffsetDateTime dateTime) {
    return RFC3339.format(dateTime);
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
          out.value(toRFC3339(value));
        }

        @Override
        public OffsetDateTime read(JsonReader in) throws IOException {
          return OffsetDateTime.parse(in.nextString());
        }

      }.nullSafe();
    }
  }
}
