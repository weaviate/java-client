package io.weaviate.client6.v1.internal.orm;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PropertiesBuilder<T> {
  void setNull(String property);

  void setText(String property, String value);

  void setBoolean(String property, Boolean value);

  void setLong(String property, Long value);

  void setDouble(String property, Double value);

  void setBlob(String property, String value);

  void setOffsetDateTime(String property, OffsetDateTime value);

  void setUuid(String property, UUID value);

  void setTextArray(String property, List<String> value);

  void setLongArray(String property, List<Long> value);

  void setDoubleArray(String property, List<Double> value);

  void setUuidArray(String property, List<UUID> value);

  void setBooleanArray(String property, List<Boolean> value);

  void setOffsetDateTimeArray(String property, List<OffsetDateTime> value);

  void setNestedObject(String property, Object value);

  T build();
}
