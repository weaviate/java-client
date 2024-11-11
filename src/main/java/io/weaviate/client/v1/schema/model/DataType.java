package io.weaviate.client.v1.schema.model;

public interface DataType {
  String CREF = "cref";
  /**
   * As of Weaviate v1.19 'string' is deprecated and replaced by 'text'.<br>
   * See <a href="https://weaviate.io/developers/weaviate/config-refs/datatypes#introduction">data types</a>
   */
  @Deprecated
  String STRING = "string";
  String TEXT = "text";
  String INT = "int";
  String NUMBER = "number";
  String BOOLEAN = "boolean";
  String DATE = "date";
  String GEO_COORDINATES = "geoCoordinates";
  String PHONE_NUMBER = "phoneNumber";
  String UUID = "uuid";
  String OBJECT = "object";
  String BLOB = "blob";

  /**
   * As of Weaviate v1.19 'string[]' is deprecated and replaced by 'text[]'.<br>
   * See <a href="https://weaviate.io/developers/weaviate/config-refs/datatypes#introduction">data types</a>
   */
  @Deprecated
  String STRING_ARRAY = "string[]";
  String TEXT_ARRAY = "text[]";
  String INT_ARRAY = "int[]";
  String NUMBER_ARRAY = "number[]";
  String BOOLEAN_ARRAY = "boolean[]";
  String DATE_ARRAY = "date[]";
  String UUID_ARRAY = "uuid[]";
  String OBJECT_ARRAY = "object[]";
}
