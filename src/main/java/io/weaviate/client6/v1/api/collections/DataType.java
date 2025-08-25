package io.weaviate.client6.v1.api.collections;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface DataType {
  public static final String TEXT = "text";
  public static final String TEXT_ARRAY = "text[]";
  public static final String INT = "int";
  public static final String NUMBER = "number";
  public static final String BOOL = "boolean";
  public static final String BOOL_ARRAY = "boolean[]";
  public static final String BLOB = "blob";
  public static final String DATE = "date";
  public static final String UUID = "uuid";
  public static final String UUID_ARRAY = "uuid[]";

  public static final Set<String> KNOWN_TYPES = ImmutableSet.of(
      TEXT, INT, BLOB, BOOL, DATE, UUID, NUMBER,
      TEXT_ARRAY, BOOL_ARRAY, UUID_ARRAY);
}
