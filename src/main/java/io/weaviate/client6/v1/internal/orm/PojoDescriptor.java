package io.weaviate.client6.v1.internal.orm;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.annotations.Collection;
import io.weaviate.client6.v1.internal.ObjectBuilder;

final class PojoDescriptor<T extends Record> implements CollectionDescriptor<T> {
  private static final Map<Class<?>, Function<String, Property>> CTORS;

  static {
    Map<Class<?>, Function<String, Property>> ctors = new HashMap<>() {
      {
        put(String.class, Property::text);
        put(String[].class, Property::textArray);

        put(OffsetDateTime.class, Property::date);
        put(OffsetDateTime[].class, Property::dateArray);

        put(UUID.class, Property::uuid);
        put(UUID[].class, Property::uuidArray);

        put(boolean.class, Property::bool);
        put(Boolean.class, Property::bool);
        put(boolean[].class, Property::boolArray);
        put(Boolean[].class, Property::boolArray);

        put(short.class, Property::integer);
        put(Short.class, Property::integer);
        put(int.class, Property::integer);
        put(Integer.class, Property::integer);
        put(long.class, Property::integer);
        put(Long.class, Property::integer);

        put(short[].class, Property::integerArray);
        put(Short[].class, Property::integerArray);
        put(int[].class, Property::integerArray);
        put(Integer[].class, Property::integerArray);
        put(long[].class, Property::integerArray);
        put(Long[].class, Property::integerArray);

        put(float.class, Property::number);
        put(Float.class, Property::number);
        put(double.class, Property::number);
        put(Double.class, Property::number);

        put(float[].class, Property::numberArray);
        put(Float[].class, Property::numberArray);
        put(double[].class, Property::numberArray);
        put(Double[].class, Property::numberArray);
      }
    };
    CTORS = Collections.unmodifiableMap(ctors);
  }

  private final Class<T> cls;

  PojoDescriptor(Class<T> cls) {
    this.cls = cls;
  }

  @Override
  public String name() {
    var annotation = cls.getAnnotation(Collection.class);
    if (annotation != null) {
      return annotation.value();
    }
    return cls.getSimpleName();
  }

  @Override
  public TypeToken<T> typeToken() {
    return TypeToken.get(cls);
  }

  @Override
  public PropertiesReader<T> propertiesReader(T properties) {
    return new PojoReader<>(properties);
  }

  @Override
  public PropertiesBuilder<T> propertiesBuilder() {
    return new PojoBuilder<>(cls);
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

      if (type == List.class) {
        var ptype = (ParameterizedType) field.getGenericType();
        var argtype = (Class<?>) ptype.getActualTypeArguments()[0];
        var arr = Array.newInstance(argtype, 0).getClass();
        ctor = CTORS.get(arr);
      } else {
        ctor = CTORS.get(type);
      }

      if (ctor == null) {
        throw new IllegalArgumentException(type.getCanonicalName() + " fields are not supported");
      }

      assert ctor != null;
      b.properties(ctor.apply(propertyName));
    }
    return b;
  }

}
