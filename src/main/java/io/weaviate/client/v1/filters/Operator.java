package io.weaviate.client.v1.filters;

public interface Operator {

  String And = "And";
  String Like = "Like";
  String Or = "Or";
  String Equal = "Equal";
  String Not = "Not";
  String NotEqual = "NotEqual";
  String GreaterThan = "GreaterThan";
  String GreaterThanEqual = "GreaterThanEqual";
  String LessThan = "LessThan";
  String LessThanEqual = "LessThanEqual";
  String WithinGeoRange = "WithinGeoRange";
  String ContainsAny = "ContainsAny";
  String ContainsAll = "ContainsAll";
  String ContainsNone = "ContainsNone";
}
