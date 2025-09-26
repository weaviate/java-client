package io.weaviate.client6.v1.api.collections.query;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase.Filters;

public class Where implements WhereOperand {

  private enum Operator {
    // Logical operators
    AND("And", WeaviateProtoBase.Filters.Operator.OPERATOR_AND),
    OR("Or", WeaviateProtoBase.Filters.Operator.OPERATOR_OR),
    NOT("Noe", WeaviateProtoBase.Filters.Operator.OPERATOR_NOT),

    // Comparison operators
    EQUAL("Equal", WeaviateProtoBase.Filters.Operator.OPERATOR_EQUAL),
    NOT_EQUAL("NotEqual", WeaviateProtoBase.Filters.Operator.OPERATOR_NOT_EQUAL),
    LESS_THAN("LessThen", WeaviateProtoBase.Filters.Operator.OPERATOR_LESS_THAN),
    LESS_THAN_EQUAL("LessThenEqual", WeaviateProtoBase.Filters.Operator.OPERATOR_LESS_THAN_EQUAL),
    GREATER_THAN("GreaterThen", WeaviateProtoBase.Filters.Operator.OPERATOR_GREATER_THAN),
    GREATER_THAN_EQUAL("GreaterThenEqual", WeaviateProtoBase.Filters.Operator.OPERATOR_GREATER_THAN_EQUAL),
    LIKE("Like", WeaviateProtoBase.Filters.Operator.OPERATOR_LIKE),
    CONTAINS_ANY("ContainsAny", WeaviateProtoBase.Filters.Operator.OPERATOR_CONTAINS_ANY),
    CONTAINS_ALL("ContainsAll", WeaviateProtoBase.Filters.Operator.OPERATOR_CONTAINS_ALL),
    CONTAINS_NONE("ContainsNone", WeaviateProtoBase.Filters.Operator.OPERATOR_CONTAINS_NONE),
    WITHIN_GEO_RANGE("WithinGeoRange", WeaviateProtoBase.Filters.Operator.OPERATOR_WITHIN_GEO_RANGE);

    /** String representation for better debug logs. */
    private final String stringValue;

    /** gRPC operator value . */
    private final WeaviateProtoBase.Filters.Operator grpcValue;

    private Operator(String stringValue, WeaviateProtoBase.Filters.Operator grpcValue) {
      this.stringValue = stringValue;
      this.grpcValue = grpcValue;
    }

    public void appendTo(Filters.Builder where) {
      where.setOperator(grpcValue);
    }

    @Override
    public String toString() {
      return stringValue;
    }
  }

  private final Operator operator;
  private final List<WhereOperand> operands;

  @SafeVarargs
  private Where(Operator operator, WhereOperand... operands) {
    this(operator, Arrays.asList(operands));
  }

  private Where(Operator operator, List<WhereOperand> operands) {
    this.operator = operator;
    this.operands = operands;
  }

  @Override
  public boolean isEmpty() {
    // Guard against Where.and(Where.or(), Where.and(), Where.not()) situation.
    return operands.isEmpty()
        || operands.stream().allMatch(operator -> operator == null | operator.isEmpty());
  }

  @Override
  public String toString() {
    if (operator == Operator.NOT) {
      return "%s %s".formatted(operator, operands.get(0));
    }
    var operandStrings = operands.stream().map(Object::toString).toList();
    return "Where(" + String.join(" " + operator.toString() + " ", operandStrings) + ")";
  }

  // Logical operators return a complete operand.
  // --------------------------------------------------------------------------
  public static Where and(final WhereOperand... operands) {
    return new Where(Operator.AND, operands);
  }

  public static Where and(final List<WhereOperand> operands) {
    return new Where(Operator.AND, operands);
  }

  public static Where or(final WhereOperand... operands) {
    return new Where(Operator.OR, operands);
  }

  public static Where or(final List<WhereOperand> operands) {
    return new Where(Operator.OR, operands);
  }

  public static Where not(final WhereOperand operand) {
    return new Where(Operator.NOT, operand);
  }

  /** Negate this expression. */
  public Where not() {
    return not(this);
  }

  // Comparison operators return fluid builder.
  // --------------------------------------------------------------------------

  public static WhereBuilder uuid() {
    return property(ById.ID_PROPERTY);
  }

  public static WhereBuilder property(String property) {
    return new WhereBuilder(new PathOperand(property));
  }

  public static WhereBuilder reference(String... path) {
    return new WhereBuilder(new PathOperand(path));
  }

  public static class WhereBuilder {
    private final WhereOperand left;

    private WhereBuilder(WhereOperand left) {
      this.left = left;
    }

    // Equal
    // ------------------------------------------------------------------------
    public Where eq(String value) {
      return new Where(Operator.EQUAL, left, new TextOperand(value));
    }

    public Where eq(String... values) {
      return new Where(Operator.EQUAL, left, new TextArrayOperand(values));
    }

    public Where eq(boolean value) {
      return new Where(Operator.EQUAL, left, new BooleanOperand(value));
    }

    public Where eq(Boolean... values) {
      return new Where(Operator.EQUAL, left, new BooleanArrayOperand(values));
    }

    public Where eq(long value) {
      return new Where(Operator.EQUAL, left, new IntegerOperand(value));
    }

    public Where eq(int value) {
      return new Where(Operator.EQUAL, left, new IntegerOperand(value));
    }

    public Where eq(Long... values) {
      return new Where(Operator.EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where eq(double value) {
      return new Where(Operator.EQUAL, left, new NumberOperand(value));
    }

    public Where eq(float value) {
      return new Where(Operator.EQUAL, left, new NumberOperand(value));
    }

    public Where eq(Double... values) {
      return new Where(Operator.EQUAL, left, new NumberArrayOperand(values));
    }

    public Where eq(OffsetDateTime value) {
      return new Where(Operator.EQUAL, left, new DateOperand(value));
    }

    public Where eq(OffsetDateTime... values) {
      return new Where(Operator.EQUAL, left, new DateArrayOperand(values));
    }

    public Where eq(Object value) {
      return new Where(Operator.EQUAL, left, fromObject(value));
    }

    // NotEqual
    // ------------------------------------------------------------------------
    public Where ne(String value) {
      return new Where(Operator.NOT_EQUAL, left, new TextOperand(value));
    }

    public Where ne(String... values) {
      return new Where(Operator.NOT_EQUAL, left, new TextArrayOperand(values));
    }

    public Where ne(boolean value) {
      return new Where(Operator.NOT_EQUAL, left, new BooleanOperand(value));
    }

    public Where ne(Boolean... values) {
      return new Where(Operator.NOT_EQUAL, left, new BooleanArrayOperand(values));
    }

    public Where ne(long value) {
      return new Where(Operator.NOT_EQUAL, left, new IntegerOperand(value));
    }

    public Where ne(int value) {
      return new Where(Operator.NOT_EQUAL, left, new IntegerOperand(value));
    }

    public Where ne(Long... values) {
      return new Where(Operator.NOT_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where ne(double value) {
      return new Where(Operator.NOT_EQUAL, left, new NumberOperand(value));
    }

    public Where ne(float value) {
      return new Where(Operator.NOT_EQUAL, left, new NumberOperand(value));
    }

    public Where ne(Double... values) {
      return new Where(Operator.NOT_EQUAL, left, new NumberArrayOperand(values));
    }

    public Where ne(OffsetDateTime value) {
      return new Where(Operator.NOT_EQUAL, left, new DateOperand(value));
    }

    public Where ne(OffsetDateTime... values) {
      return new Where(Operator.NOT_EQUAL, left, new DateArrayOperand(values));
    }

    public Where ne(Object value) {
      return new Where(Operator.NOT_EQUAL, left, fromObject(value));
    }

    // LessThan
    // ------------------------------------------------------------------------
    public Where lt(String value) {
      return new Where(Operator.LESS_THAN, left, new TextOperand(value));
    }

    public Where lt(String... values) {
      return new Where(Operator.LESS_THAN, left, new TextArrayOperand(values));
    }

    public Where lt(long value) {
      return new Where(Operator.LESS_THAN, left, new IntegerOperand(value));
    }

    public Where lt(int value) {
      return new Where(Operator.LESS_THAN, left, new IntegerOperand(value));
    }

    public Where lt(Long... values) {
      return new Where(Operator.LESS_THAN, left, new IntegerArrayOperand(values));
    }

    public Where lt(double value) {
      return new Where(Operator.LESS_THAN, left, new NumberOperand(value));
    }

    public Where lt(float value) {
      return new Where(Operator.LESS_THAN, left, new NumberOperand(value));
    }

    public Where lt(Double... values) {
      return new Where(Operator.LESS_THAN, left, new NumberArrayOperand(values));
    }

    public Where lt(OffsetDateTime value) {
      return new Where(Operator.LESS_THAN, left, new DateOperand(value));
    }

    public Where lt(OffsetDateTime... values) {
      return new Where(Operator.LESS_THAN, left, new DateArrayOperand(values));
    }

    public Where lt(Object value) {
      return new Where(Operator.LESS_THAN, left, fromObject(value));
    }

    // LessThanEqual
    // ------------------------------------------------------------------------
    public Where lte(String value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new TextOperand(value));
    }

    public Where lte(String... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new TextArrayOperand(values));
    }

    public Where lte(long value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Where lte(int value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Where lte(Long... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where lte(double value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Where lte(float value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Where lte(Double... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new NumberArrayOperand(values));
    }

    public Where lte(OffsetDateTime value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new DateOperand(value));
    }

    public Where lte(OffsetDateTime... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new DateArrayOperand(values));
    }

    public Where lte(Object value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, fromObject(value));
    }

    // GreaterThan
    // ------------------------------------------------------------------------
    public Where gt(String value) {
      return new Where(Operator.GREATER_THAN, left, new TextOperand(value));
    }

    public Where gt(String... values) {
      return new Where(Operator.GREATER_THAN, left, new TextArrayOperand(values));
    }

    public Where gt(long value) {
      return new Where(Operator.GREATER_THAN, left, new IntegerOperand(value));
    }

    public Where gt(int value) {
      return new Where(Operator.GREATER_THAN, left, new IntegerOperand(value));
    }

    public Where gt(Long... values) {
      return new Where(Operator.GREATER_THAN, left, new IntegerArrayOperand(values));
    }

    public Where gt(double value) {
      return new Where(Operator.GREATER_THAN, left, new NumberOperand(value));
    }

    public Where gt(float value) {
      return new Where(Operator.GREATER_THAN, left, new NumberOperand(value));
    }

    public Where gt(Double... values) {
      return new Where(Operator.GREATER_THAN, left, new NumberArrayOperand(values));
    }

    public Where gt(OffsetDateTime value) {
      return new Where(Operator.GREATER_THAN, left, new DateOperand(value));
    }

    public Where gt(OffsetDateTime... values) {
      return new Where(Operator.GREATER_THAN, left, new DateArrayOperand(values));
    }

    public Where gt(Object value) {
      return new Where(Operator.GREATER_THAN, left, fromObject(value));
    }

    // GreaterThanEqual
    // ------------------------------------------------------------------------
    public Where gte(String value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new TextOperand(value));
    }

    public Where gte(String... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new TextArrayOperand(values));
    }

    public Where gte(long value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Where gte(int value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Where gte(Long... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where gte(double value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Where gte(float value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Where gte(Double... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new NumberArrayOperand(values));
    }

    public Where gte(OffsetDateTime value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new DateOperand(value));
    }

    public Where gte(OffsetDateTime... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new DateArrayOperand(values));
    }

    public Where gte(Object value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, fromObject(value));
    }

    // Like
    // ------------------------------------------------------------------------
    public Where like(String value) {
      return new Where(Operator.LIKE, left, new TextOperand(value));
    }

    // ContainsAny
    // ------------------------------------------------------------------------
    public Where containsAny(String value) {
      return new Where(Operator.CONTAINS_ANY, left, new TextOperand(value));
    }

    public Where containsAny(String... values) {
      return new Where(Operator.CONTAINS_ANY, left, new TextArrayOperand(values));
    }

    public Where containsAny(Boolean... values) {
      return new Where(Operator.CONTAINS_ANY, left, new BooleanArrayOperand(values));
    }

    public Where containsAny(Long... values) {
      return new Where(Operator.CONTAINS_ANY, left, new IntegerArrayOperand(values));
    }

    public Where containsAny(Double... values) {
      return new Where(Operator.CONTAINS_ANY, left, new NumberArrayOperand(values));
    }

    public Where containsAny(OffsetDateTime... values) {
      return new Where(Operator.CONTAINS_ANY, left, new DateArrayOperand(values));
    }

    // ContainsAll
    // ------------------------------------------------------------------------
    public Where containsAll(String value) {
      return new Where(Operator.CONTAINS_ALL, left, new TextOperand(value));
    }

    public Where containsAll(String... values) {
      return new Where(Operator.CONTAINS_ALL, left, new TextArrayOperand(values));
    }

    public Where containsAll(Boolean... values) {
      return new Where(Operator.CONTAINS_ALL, left, new BooleanArrayOperand(values));
    }

    public Where containsAll(Long... values) {
      return new Where(Operator.CONTAINS_ALL, left, new IntegerArrayOperand(values));
    }

    public Where containsAll(Double... values) {
      return new Where(Operator.CONTAINS_ALL, left, new NumberArrayOperand(values));
    }

    public Where containsAll(OffsetDateTime... values) {
      return new Where(Operator.CONTAINS_ALL, left, new DateArrayOperand(values));
    }

    // ContainsNone
    // ------------------------------------------------------------------------
    public Where containsNone(String value) {
      return new Where(Operator.CONTAINS_NONE, left, new TextOperand(value));
    }

    public Where containsNone(String... values) {
      return new Where(Operator.CONTAINS_NONE, left, new TextArrayOperand(values));
    }

    public Where containsNone(Boolean... values) {
      return new Where(Operator.CONTAINS_NONE, left, new BooleanArrayOperand(values));
    }

    public Where containsNone(Long... values) {
      return new Where(Operator.CONTAINS_NONE, left, new IntegerArrayOperand(values));
    }

    public Where containsNone(Double... values) {
      return new Where(Operator.CONTAINS_NONE, left, new NumberArrayOperand(values));
    }

    public Where containsNone(OffsetDateTime... values) {
      return new Where(Operator.CONTAINS_NONE, left, new DateArrayOperand(values));
    }

    // WithinGeoRange
    // ------------------------------------------------------------------------
    public Where withinGeoRange(float lat, float lon, float maxDistance) {
      return new Where(Operator.WITHIN_GEO_RANGE, left, new GeoRangeOperand(lat, lon, maxDistance));
    }
  }

  @Override
  public void appendTo(WeaviateProtoBase.Filters.Builder where) {
    if (isEmpty()) {
      return;
    }

    operator.appendTo(where);

    if (operator == Operator.AND || operator == Operator.OR || operator == Operator.NOT) {
      operands.forEach(op -> {
        var nested = Filters.newBuilder();
        op.appendTo(nested);
        where.addFilters(nested);
      });
    } else {
      // Comparison operators: eq, gt, lt, like, etc.
      operands.forEach(op -> op.appendTo(where));
    }
  }

  @SuppressWarnings("unchecked")
  static WhereOperand fromObject(Object value) {
    if (value instanceof String str) {
      return new TextOperand(str);
    } else if (value instanceof Boolean bool) {
      return new BooleanOperand(bool);
    } else if (value instanceof Long l) {
      return new IntegerOperand(l);
    } else if (value instanceof Integer i) {
      return new IntegerOperand(i);
    } else if (value instanceof Double dbl) {
      return new NumberOperand(dbl);
    } else if (value instanceof Float f) {
      return new NumberOperand(f);
    } else if (value instanceof OffsetDateTime date) {
      return new DateOperand(date);
    } else if (value instanceof String[] strarr) {
      return new TextArrayOperand(strarr);
    } else if (value instanceof Boolean[] boolarr) {
      return new BooleanArrayOperand(boolarr);
    } else if (value instanceof Long[] lngarr) {
      return new IntegerArrayOperand(lngarr);
    } else if (value instanceof Double[] dblarr) {
      return new NumberArrayOperand(dblarr);
    } else if (value instanceof OffsetDateTime[] datearr) {
      return new DateArrayOperand(datearr);
    } else if (value instanceof List<?> list) {
      if (list.isEmpty()) {
        throw new IllegalArgumentException(
            "Filter with non-reifiable type (List<T>) cannot be empty, use an array instead");
      }

      Object first = list.get(0);
      if (first instanceof String) {
        return new TextArrayOperand((List<String>) value);
      } else if (first instanceof Boolean) {
        return new BooleanArrayOperand((List<Boolean>) value);
      } else if (first instanceof Long) {
        return new IntegerArrayOperand((List<Long>) value);
      } else if (first instanceof Double) {
        return new NumberArrayOperand((List<Double>) value);
      } else if (first instanceof OffsetDateTime) {
        return new DateArrayOperand((List<OffsetDateTime>) value);
      }
    }
    throw new IllegalArgumentException(
        "value must be either of String, Boolean, OffsetDateTime, Long, Double, or Array/List of these types");
  }

  private static class PathOperand implements WhereOperand {
    private final List<String> path;

    private PathOperand(List<String> path) {
      this.path = path;
    }

    private PathOperand(String... path) {
      this(Arrays.asList(path));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      // "on" is deprecated, but the current proto doesn"t have "path".
      if (!path.isEmpty()) {
        where.addOn(path.get(0));
      }
      // FIXME: no way to reference objects rn?
    }

    @Override
    public String toString() {
      return String.join("::", path);
    }
  }

  private static class TextOperand implements WhereOperand {
    private final String value;

    private TextOperand(String value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueText(value);
    }

    @Override
    public String toString() {
      return value;
    }
  }

  private static class TextArrayOperand implements WhereOperand {
    private final List<String> values;

    private TextArrayOperand(List<String> values) {
      this.values = values;
    }

    @SafeVarargs
    private TextArrayOperand(String... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class BooleanOperand implements WhereOperand {
    private final boolean value;

    private BooleanOperand(boolean value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueBoolean(value);
    }

    @Override
    public String toString() {
      return Boolean.toString(value);
    }
  }

  private static class BooleanArrayOperand implements WhereOperand {
    private final List<Boolean> values;

    private BooleanArrayOperand(List<Boolean> values) {
      this.values = values;
    }

    @SafeVarargs
    private BooleanArrayOperand(Boolean... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueBooleanArray(WeaviateProtoBase.BooleanArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class IntegerOperand implements WhereOperand {
    private final long value;

    private IntegerOperand(long value) {
      this.value = value;
    }

    private IntegerOperand(int value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueInt(value);
    }

    @Override
    public String toString() {
      return Long.toString(value);
    }
  }

  private static class IntegerArrayOperand implements WhereOperand {
    private final List<Long> values;

    private IntegerArrayOperand(List<Long> values) {
      this.values = values;
    }

    @SafeVarargs
    private IntegerArrayOperand(Long... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueIntArray(WeaviateProtoBase.IntArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class NumberOperand implements WhereOperand {
    private final double value;

    private NumberOperand(double value) {
      this.value = value;
    }

    private NumberOperand(float value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueNumber(value);
    }

    @Override
    public String toString() {
      return Double.toString(value);
    }
  }

  private static class NumberArrayOperand implements WhereOperand {
    private final List<Double> values;

    private NumberArrayOperand(List<Double> values) {
      this.values = values;
    }

    @SafeVarargs
    private NumberArrayOperand(Double... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueNumberArray(WeaviateProtoBase.NumberArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class DateOperand implements WhereOperand {
    private final OffsetDateTime value;

    private DateOperand(OffsetDateTime value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueText(value.toString());
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }

  private static class DateArrayOperand implements WhereOperand {
    private final List<OffsetDateTime> values;

    private DateArrayOperand(List<OffsetDateTime> values) {
      this.values = values;
    }

    @SafeVarargs
    private DateArrayOperand(OffsetDateTime... values) {
      this(Arrays.asList(values));
    }

    private List<String> formatted() {
      return values.stream().map(OffsetDateTime::toString).toList();
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(formatted()));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class GeoRangeOperand implements WhereOperand {
    private final Float lat;
    private final Float lon;
    private final Float distance;

    private GeoRangeOperand(Float lat, Float lon, Float distance) {
      this.lat = lat;
      this.lon = lon;
      this.distance = distance;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueGeo(WeaviateProtoBase.GeoCoordinatesFilter.newBuilder()
          .setLatitude(lat).setLongitude(lon).setDistance(distance));
    }

    @Override
    public String toString() {
      return "(lat=%d, lon=%d, distance=%d)".formatted(lat, lon, distance);
    }
  }
}
