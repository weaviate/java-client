package io.weaviate.client6.v1.internal.orm;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.internal.ObjectBuilder;

final class PojoDescriptor<T> implements CollectionDescriptor<T> {
  private static final Map<Class<?>, Function<String, Property>> ctors;

  static {
    Map<Class<?>, Function<String, Property>> _ctors = new HashMap<>() {
      {
      }
    };
    ctors = Collections.unmodifiableMap(_ctors);
  }

  private final Class<T> cls;

  PojoDescriptor(Class<T> cls) {
    this.cls = cls;
  }

  @Override
  public String name() {
    return cls.getSimpleName();
  }

  @Override
  public TypeToken<T> typeToken() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'typeToken'");
  }

  @Override
  public PropertiesReader<T> propertiesReader(T properties) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'propertiesReader'");
  }

  @Override
  public PropertiesBuilder<T> propertiesBuilder() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'propertiesBuilder'");
  }

  @Override
  public Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> configFn() {
    return this::inspectClass;
  }

  private ObjectBuilder<CollectionConfig> inspectClass(CollectionConfig.Builder b) {
    // Add properties;
    for (var field : cls.getDeclaredFields()) {
      var propertyName = field.getName();
      Function<String, Property> ctor;
      var type = field.getType();
      if (type == String.class) {
        ctor = Property::text;
      } else if (type == String[].class) {
        ctor = Property::textArray;
      } else if (type == short.class || type == Short.class
          || type == int.class || type == Integer.class
          || type == long.class || type == Long.class) {
        ctor = Property::integer;
      } else if (type == short[].class || type == Short[].class
          || type == int[].class || type == Integer[].class
          || type == long[].class || type == Long[].class) {
        ctor = Property::integerArray;
      } else if (type == float.class || type == Float.class
          || type == double.class || type == Double.class) {
        ctor = Property::number;
      } else if (type == float[].class || type == Float[].class
          || type == double[].class || type == Double[].class) {
        ctor = Property::numberArray;
      } else if (type == List.class) {
        var ptype = (ParameterizedType) field.getGenericType();
        var ltype = (Class<?>) ptype.getActualTypeArguments()[0];
        if (ltype == String.class) {
          ctor = Property::textArray;
        } else if (ltype == short.class || ltype == Short.class
            || ltype == int.class || ltype == Integer.class
            || ltype == long.class || ltype == Long.class) {
          ctor = Property::integerArray;
        } else if (ltype == float.class || ltype == Float.class
            || ltype == double.class || ltype == Double.class) {
          ctor = Property::numberArray;
        } else {
          throw new IllegalArgumentException(ltype.getCanonicalName() + " is not supported");
        }
      } else {
        throw new IllegalArgumentException(type.getCanonicalName() + " is not supported");
      }
      b.properties(ctor.apply(propertyName));
    }
    return b;
  }

}
