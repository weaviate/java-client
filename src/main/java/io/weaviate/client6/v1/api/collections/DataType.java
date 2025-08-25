package io.weaviate.client6.v1.api.collections;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface DataType {
  public static final String TEXT = "text";
  public static final String INT = "int";
  public static final String BLOB = "blob";
  public static final String BOOL = "boolean";

  public static final Set<String> KNOWN_TYPES = ImmutableSet.of(TEXT, INT, BLOB, BOOL);
}
