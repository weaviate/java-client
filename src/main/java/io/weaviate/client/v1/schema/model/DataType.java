package io.weaviate.client.v1.schema.model;

public interface DataType {
  String CREF = "cref";
  @Deprecated
  String STRING = "string";
  String TEXT = "text";
  String INT = "int";
  String NUMBER = "number";
  String BOOLEAN = "boolean";
  String DATE = "date";
  String GEO_COORDINATES = "geoCoordinates";
  String PHONE_NUMBER = "phoneNumber";
  @Deprecated
  String STRING_ARRAY = "string[]";
  String TEXT_ARRAY = "text[]";
  String INT_ARRAY = "int[]";
  String NUMBER_ARRAY = "number[]";
  String BOOLEAN_ARRAY = "boolean[]";
  String DATE_ARRAY = "date[]";
}
