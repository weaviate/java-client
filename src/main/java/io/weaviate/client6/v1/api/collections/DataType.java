package io.weaviate.client6.v1.api.collections;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface DataType {
  public static final String TEXT = "text";
  public static final String TEXT_ARRAY = "text[]";
  public static final String INT = "int";
  public static final String INT_ARRAY = "int[]";
  public static final String NUMBER = "number";
  public static final String NUMBER_ARRAY = "number[]";
  public static final String BOOL = "boolean";
  public static final String BOOL_ARRAY = "boolean[]";
  public static final String BLOB = "blob";
  public static final String DATE = "date";
  public static final String DATE_ARRAY = "date[]";
  public static final String UUID = "uuid";
  public static final String UUID_ARRAY = "uuid[]";
  public static final String OBJECT = "object";
  public static final String OBJECT_ARRAY = "object[]";
  public static final String PHONE_NUMBER = "phoneNumber";
  public static final String GEO_COORDINATES = "geoCoordinates";

  /**
   * Scalar/array types which Weaviate and WeaviateClient recognize.
   *
   * <p>
   * Other data types are considered reference types, i.e. if a user creates a
   * property with type {@code "timestamp"}, the client will count it a
   * cross-reference to the {@code "timestamp"} collection.
   *
   * This is obviously wrong, so it is recommended to always create properties
   * using {@link Property}'s factory classes.
   */
  public static final Set<String> KNOWN_TYPES = ImmutableSet.of(
      TEXT, INT, BLOB, BOOL, DATE, UUID, NUMBER, OBJECT,
      TEXT_ARRAY, INT_ARRAY, NUMBER_ARRAY, BOOL_ARRAY, DATE_ARRAY, UUID_ARRAY, OBJECT_ARRAY,
      PHONE_NUMBER, GEO_COORDINATES);
}
