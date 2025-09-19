package io.weaviate.client6.v1.internal.orm;

import java.lang.reflect.Constructor;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

final class PojoBuilder<PropertiesT extends Record> implements PropertiesBuilder<PropertiesT> {
  private final PojoDescriptor<PropertiesT> descriptor;
  private final Constructor<PropertiesT> ctor;
  private final Map<String, Arg> ctorArgs;

  static record Arg(Class<?> type, Object value) {
    Arg withValue(Object value) {
      return new Arg(this.type, value);
    }
  }

  PojoBuilder(PojoDescriptor<PropertiesT> descriptor) {
    this.descriptor = descriptor;

    var args = descriptor._class().getRecordComponents();
    ctorArgs = new LinkedHashMap<String, Arg>(args.length);

    var componentTypes = Arrays.stream(args)
        .map(arg -> {
          // LinkedHashMap allows null values.
          var type = arg.getType();
          ctorArgs.put(arg.getName(), new Arg(type, null));
          return type;
        })
        .toArray(Class<?>[]::new);
    try {
      ctor = descriptor._class().getDeclaredConstructor(componentTypes);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private void setValue(String propertyName, Object value) {
    var fieldName = descriptor.fieldName(propertyName);
    if (!ctorArgs.containsKey(fieldName)) {
      return;
    }
    var arg = ctorArgs.get(fieldName);
    ctorArgs.put(fieldName, arg.withValue(value));
  }

  private Class<?> getArgType(String propertyName) {
    return ctorArgs.get(descriptor.fieldName(propertyName)).type();
  }

  private boolean isArray(String propertyName, Class<?>... classes) {
    var type = getArgType(propertyName);
    if (!type.isArray()) {
      return false;
    }
    if (classes.length == 0) {
      return true;
    }
    var componentType = type.getComponentType();
    for (final var cls : classes) {
      if (componentType == cls) {
        return true;
      }
    }
    return false;
  }

  /** Is either of types. */
  private boolean isType(String propertyName, Class<?>... classes) {
    var type = getArgType(propertyName);
    for (final var cls : classes) {
      if (type == cls) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setNull(String propertyName) {
    setValue(propertyName, null);
  }

  @Override
  public void setText(String propertyName, String value) {
    setValue(propertyName, value);
  }

  @Override
  public void setBoolean(String propertyName, Boolean value) {
    if (isType(propertyName, boolean.class)) {
      setValue(propertyName, value.booleanValue());
    } else {
      setValue(propertyName, value);
    }
  }

  @Override
  // TODO: rename to setLong
  public void setInteger(String propertyName, Long value) {
    if (isType(propertyName, short.class, Short.class)) {
      setValue(propertyName, value.shortValue());
    } else if (isType(propertyName, int.class, Integer.class)) {
      setValue(propertyName, value.intValue());
    } else {
      setValue(propertyName, value);
    }
  }

  @Override
  public void setDouble(String propertyName, Double value) {
    if (isType(propertyName, float.class, Float.class)) {
      setValue(propertyName, value.floatValue());
    } else {
      setValue(propertyName, value);
    }
  }

  @Override
  public void setBlob(String propertyName, String value) {
    setValue(propertyName, value);
  }

  @Override
  public void setOffsetDateTime(String propertyName, OffsetDateTime value) {
    setValue(propertyName, value);
  }

  @Override
  public void setUuid(String propertyName, UUID value) {
    setValue(propertyName, value);
  }

  @Override
  public void setTextArray(String propertyName, List<String> value) {
    setValue(propertyName, isArray(propertyName)
        ? value.toArray(String[]::new)
        : value);

  }

  @Override
  public void setLongArray(String propertyName, List<Long> value) {
    if (isArray(propertyName, short.class)) {
      setValue(propertyName, ArrayUtils.toPrimitive(value.stream().map(Long::shortValue).toArray(Short[]::new)));
    } else if (isArray(propertyName, Short.class)) {
      setValue(propertyName, value.stream().map(Long::shortValue).toArray(Short[]::new));
    } else if (isArray(propertyName, int.class)) {
      setValue(propertyName, ArrayUtils.toPrimitive(value.stream().map(Long::intValue).toArray(Integer[]::new)));
    } else if (isArray(propertyName, Integer.class)) {
      setValue(propertyName, value.stream().map(Long::intValue).toArray(Integer[]::new));
    } else if (isArray(propertyName, long.class)) {
      setValue(propertyName, ArrayUtils.toPrimitive(value.stream().map(Long::longValue).toArray(Long[]::new)));
    } else if (isArray(propertyName, Long.class)) {
      setValue(propertyName, value.stream().toArray(Long[]::new));
    } else {
      setValue(propertyName, value);
    }
  }

  @Override
  public void setDoubleArray(String propertyName, List<Double> value) {
    if (isArray(propertyName, float.class)) {
      setValue(propertyName, ArrayUtils.toPrimitive(value.stream().map(Double::floatValue).toArray(Float[]::new)));
    } else if (isArray(propertyName, Float.class)) {
      setValue(propertyName, value.stream().map(Double::floatValue).toArray(Float[]::new));
    } else if (isArray(propertyName, double.class)) {
      setValue(propertyName, ArrayUtils.toPrimitive(value.stream().map(Double::doubleValue).toArray(Double[]::new)));
    } else if (isArray(propertyName, Double.class)) {
      setValue(propertyName, value.stream().toArray(Double[]::new));
    } else {
      setValue(propertyName, value);
    }
  }

  @Override
  public void setUuidArray(String propertyName, List<UUID> value) {
    setValue(propertyName, isArray(propertyName)
        ? value.toArray(UUID[]::new)
        : value);
  }

  @Override
  public void setBooleanArray(String propertyName, List<Boolean> value) {
    if (isArray(propertyName, boolean.class)) {
      setValue(propertyName, ArrayUtils.toPrimitive(value.stream().map(Boolean::booleanValue).toArray(Boolean[]::new)));
    } else if (isArray(propertyName, Boolean.class)) {
      setValue(propertyName, value.stream().map(Boolean::booleanValue).toArray(Boolean[]::new));
    } else {
      setValue(propertyName, value);
    }
  }

  @Override
  public void setOffsetDateTimeArray(String propertyName, List<OffsetDateTime> value) {
    setValue(propertyName, isArray(propertyName)
        ? value.toArray(OffsetDateTime[]::new)
        : value);
  }

  @Override
  public PropertiesT build() {
    Object[] args = ctorArgs.values().stream().map(Arg::value).toArray();
    try {
      ctor.setAccessible(true);
      return ctor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
