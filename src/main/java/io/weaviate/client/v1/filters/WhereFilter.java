package io.weaviate.client.v1.filters;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class WhereFilter {

  WhereFilter[] operands;
  String operator;

  String[] path;
  Boolean valueBoolean;
  Date valueDate;
  GeoRange valueGeoRange;
  Integer valueInt;
  Double valueNumber;
  /**
   * As of Weaviate v1.19 'valueString' is deprecated and replaced by 'valueText'.<br>
   * See <a href="https://weaviate.io/developers/weaviate/config-refs/datatypes#introduction">data types</a>
   */
  @Deprecated
  String valueString;
  String valueText;


  @Getter
  @Builder
  @ToString
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @EqualsAndHashCode
  public static class GeoRange {

    GeoDistance distance;
    GeoCoordinates geoCoordinates;
  }

  @Getter
  @Builder
  @ToString
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @EqualsAndHashCode
  public static class GeoDistance {

    Float max;
  }

  @Getter
  @Builder
  @ToString
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @EqualsAndHashCode
  public static class GeoCoordinates {

    Float latitude;
    Float longitude;
  }
}
