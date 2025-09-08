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
  private final Constructor<PropertiesT> ctor;
  private final Map<String, Arg> ctorArgs;

  static record Arg(Class<?> type, Object value) {
    Arg withValue(Object value) {
      return new Arg(this.type, value);
    }
  }

  PojoBuilder(Class<PropertiesT> cls) {
    var args = cls.getRecordComponents();
    ctorArgs = new LinkedHashMap<String, Arg>(args.length);

    var componentTypes = Arrays.stream(args)
        .map(arg -> {
          // LinkedHahsMap allows null values.
          var type = arg.getType();
          ctorArgs.put(arg.getName(), new Arg(type, null));
          return type;
        })
        .toArray(Class<?>[]::new);
    try {
      ctor = cls.getDeclaredConstructor(componentTypes);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private void setValue(String fieldName, Object value) {
    if (!ctorArgs.containsKey(fieldName)) {
      return;
    }
    var arg = ctorArgs.get(fieldName);
    // value = coerse.apply(value);
    ctorArgs.put(fieldName, arg.withValue(value));
  }

  private Class<?> getArgType(String fieldName) {
    return ctorArgs.get(fieldName).type();
  }

  private boolean isArray(String fieldName, Class<?>... classes) {
    var type = getArgType(fieldName);
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
  private boolean isType(String fieldName, Class<?>... classes) {
    var type = getArgType(fieldName);
    for (final var cls : classes) {
      if (type == cls) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setNull(String property) {
    setValue(property, null);
  }

  @Override
  public void setText(String property, String value) {
    setValue(property, value);
  }

  @Override
  public void setBoolean(String property, Boolean value) {
    if (isType(property, boolean.class)) {
      setValue(property, value.booleanValue());
    } else {
      setValue(property, value);
    }
  }

  @Override
  // TODO: rename to setLong
  public void setInteger(String property, Long value) {
    if (isType(property, short.class, Short.class)) {
      setValue(property, value.shortValue());
    } else if (isType(property, int.class, Integer.class)) {
      setValue(property, value.intValue());
    } else {
      setValue(property, value);
    }
  }

  @Override
  public void setDouble(String property, Double value) {
    if (isType(property, float.class, Float.class)) {
      setValue(property, value.floatValue());
    } else {
      setValue(property, value);
    }
  }

  @Override
  public void setBlob(String property, String value) {
    setValue(property, value);
  }

  @Override
  public void setOffsetDateTime(String property, OffsetDateTime value) {
    setValue(property, value);
  }

  @Override
  public void setUuid(String property, UUID value) {
    setValue(property, value);
  }

  @Override
  public void setTextArray(String property, List<String> value) {
    setValue(property, isArray(property)
        ? value.toArray(String[]::new)
        : value);

  }

  @Override
  public void setLongArray(String property, List<Long> value) {
    if (isArray(property, short.class)) {
      setValue(property, ArrayUtils.toPrimitive(value.stream().map(Long::shortValue).toArray(Short[]::new)));
    } else if (isArray(property, Short.class)) {
      setValue(property, value.stream().map(Long::shortValue).toArray(Short[]::new));
    } else if (isArray(property, int.class)) {
      setValue(property, ArrayUtils.toPrimitive(value.stream().map(Long::intValue).toArray(Integer[]::new)));
    } else if (isArray(property, Integer.class)) {
      setValue(property, value.stream().map(Long::intValue).toArray(Integer[]::new));
    } else if (isArray(property, long.class)) {
      setValue(property, ArrayUtils.toPrimitive(value.stream().map(Long::longValue).toArray(Long[]::new)));
    } else if (isArray(property, Long.class)) {
      setValue(property, value.stream().toArray(Long[]::new));
    } else {
      setValue(property, value);
    }
  }

  @Override
  public void setDoubleArray(String property, List<Double> value) {
    if (isArray(property, float.class)) {
      setValue(property, ArrayUtils.toPrimitive(value.stream().map(Double::floatValue).toArray(Float[]::new)));
    } else if (isArray(property, Float.class)) {
      setValue(property, value.stream().map(Double::floatValue).toArray(Float[]::new));
    } else if (isArray(property, double.class)) {
      setValue(property, ArrayUtils.toPrimitive(value.stream().map(Double::doubleValue).toArray(Double[]::new)));
    } else if (isArray(property, Double.class)) {
      setValue(property, value.stream().toArray(Double[]::new));
    } else {
      setValue(property, value);
    }
  }

  @Override
  public void setUuidArray(String property, List<UUID> value) {
    setValue(property, isArray(property)
        ? value.toArray(UUID[]::new)
        : value);
  }

  @Override
  public void setBooleanArray(String property, List<Boolean> value) {
    if (isArray(property, boolean.class)) {
      setValue(property, ArrayUtils.toPrimitive(value.stream().map(Boolean::booleanValue).toArray(Boolean[]::new)));
    } else if (isArray(property, Boolean.class)) {
      setValue(property, value.stream().map(Boolean::booleanValue).toArray(Boolean[]::new));
    } else {
      setValue(property, value);
    }
  }

  @Override
  public void setOffsetDateTimeArray(String property, List<OffsetDateTime> value) {
    setValue(property, isArray(property)
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
