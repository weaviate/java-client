package io.weaviate.client6.v1.internal.orm;

import java.lang.reflect.Field;

import com.google.gson.FieldNamingStrategy;

public enum PropertyFieldNamingStrategy implements FieldNamingStrategy {
  INSTANCE;

  @Override
  public String translateName(Field field) {
    return PojoDescriptor.propertyName(field);
  }
}
