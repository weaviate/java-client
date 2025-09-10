package io.weaviate.client.v1.filters;

import java.util.Date;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class WhereFilter {

  WhereFilter[] operands;
  String operator;

  String[] path;
  Boolean valueBoolean;
  Boolean[] valueBooleanArray;
  Date valueDate;
  Date[] valueDateArray;
  GeoRange valueGeoRange;
  Integer valueInt;
  Integer[] valueIntArray;
  Double valueNumber;
  Double[] valueNumberArray;
  /**
   * As of Weaviate v1.19 'valueString' is deprecated and replaced by
   * 'valueText'.<br>
   * See <a href=
   * "https://weaviate.io/developers/weaviate/config-refs/datatypes#introduction">data
   * types</a>
   */
  @Deprecated
  String valueString;
  @Deprecated
  String[] valueStringArray;
  String valueText;
  String[] valueTextArray;

  public static WhereFilterBuilder builder() {
    return new WhereFilterBuilder();
  }

  public static class WhereFilterBuilder {
    private WhereFilter[] operands;
    private String operator;
    private String[] path;

    private Boolean[] valueBooleanArray;
    private Date[] valueDateArray;
    private Integer[] valueIntArray;
    private Double[] valueNumberArray;
    private String[] valueStringArray;
    private String[] valueTextArray;
    private GeoRange valueGeoRange;

    public WhereFilterBuilder operands(WhereFilter... operands) {
      this.operands = operands;
      return this;
    }

    public WhereFilterBuilder operator(String operator) {
      this.operator = operator;
      return this;
    }

    public WhereFilterBuilder path(String... path) {
      this.path = path;
      return this;
    }

    public WhereFilterBuilder valueBoolean(Boolean... valueBoolean) {
      valueBooleanArray = valueBoolean;
      return this;
    }

    public WhereFilterBuilder valueDate(Date... valueDate) {
      valueDateArray = valueDate;
      return this;
    }

    public WhereFilterBuilder valueInt(Integer... valueInt) {
      valueIntArray = valueInt;
      return this;
    }

    public WhereFilterBuilder valueNumber(Double... valueNumber) {
      valueNumberArray = valueNumber;
      return this;
    }

    /** Deprecated: use {@link valueText} instead. */
    @Deprecated
    public WhereFilterBuilder valueString(String... valueString) {
      valueStringArray = valueString;
      return this;
    }

    public WhereFilterBuilder valueText(String... valueText) {
      valueTextArray = valueText;
      return this;
    }

    public WhereFilterBuilder valueGeoRange(GeoRange valueGeoRange) {
      this.valueGeoRange = valueGeoRange;
      return this;
    }

    public WhereFilter build() {
      WhereFilter f = new WhereFilter();
      f.operands = operands;
      f.operator = operator;
      f.path = path;
      f.valueGeoRange = valueGeoRange;
      assignSingleOrArray(valueBooleanArray, s -> f.valueBoolean = s, a -> f.valueBooleanArray = a);
      assignSingleOrArray(valueDateArray, s -> f.valueDate = s, a -> f.valueDateArray = a);
      assignSingleOrArray(valueIntArray, s -> f.valueInt = s, a -> f.valueIntArray = a);
      assignSingleOrArray(valueNumberArray, s -> f.valueNumber = s, a -> f.valueNumberArray = a);
      assignSingleOrArray(valueStringArray, s -> f.valueString = s, a -> f.valueStringArray = a);
      assignSingleOrArray(valueTextArray, s -> f.valueText = s, a -> f.valueTextArray = a);

      return f;
    }

    private <T> void assignSingleOrArray(T[] values, Consumer<T> single, Consumer<T[]> array) {
      if (ArrayUtils.isNotEmpty(values)) {
        if (values.length > 1 || Operator.ContainsAny.equals(operator) || Operator.ContainsAll.equals(operator)
            || Operator.ContainsNone.equals(operator)) {
          array.accept(values);
        } else {
          single.accept(values[0]);
        }
      }
    }
  }

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
