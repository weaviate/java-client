package io.weaviate.client6.v1.api.collections.query;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase.Filters;

public class Where implements WhereOperand {

  private enum Operator {
    // Logical operators
    AND("And", WeaviateProtoBase.Filters.Operator.OPERATOR_AND),
    OR("Or", WeaviateProtoBase.Filters.Operator.OPERATOR_OR),

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
    // Guard against Where.and(Where.or(), Where.and()) situation.
    return operands.isEmpty()
        || operands.stream().allMatch(operator -> operator.isEmpty());
  }

  @Override
  public String toString() {
    var operandStrings = operands.stream().map(Object::toString).toList();
    return "Where(" + String.join(" " + operator.toString() + " ", operandStrings) + ")";
  }

  // Logical operators return a complete operand.
  // --------------------------------------------------------------------------
  public static Where and(WhereOperand... operands) {
    return new Where(Operator.AND, operands);
  }

  public static Where and(List<WhereOperand> operands) {
    return new Where(Operator.AND, operands);
  }

  public static Where or(WhereOperand... operands) {
    return new Where(Operator.OR, operands);
  }

  public static Where or(List<WhereOperand> operands) {
    return new Where(Operator.OR, operands);
  }

  // Comparison operators return fluid builder.
  // --------------------------------------------------------------------------

  public static WhereBuilder uuid() {
    return property("_id");
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

    public Where eq(Boolean value) {
      return new Where(Operator.EQUAL, left, new BooleanOperand(value));
    }

    public Where eq(Boolean... values) {
      return new Where(Operator.EQUAL, left, new BooleanArrayOperand(values));
    }

    public Where eq(Integer value) {
      return new Where(Operator.EQUAL, left, new IntegerOperand(value));
    }

    public Where eq(Integer... values) {
      return new Where(Operator.EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where eq(Number value) {
      return new Where(Operator.EQUAL, left, new NumberOperand(value.doubleValue()));
    }

    public Where eq(Number... values) {
      return new Where(Operator.EQUAL, left, new NumberArrayOperand(values));
    }

    public Where eq(Date value) {
      return new Where(Operator.EQUAL, left, new DateOperand(value));
    }

    public Where eq(Date... values) {
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

    public Where ne(Boolean value) {
      return new Where(Operator.NOT_EQUAL, left, new BooleanOperand(value));
    }

    public Where ne(Boolean... values) {
      return new Where(Operator.NOT_EQUAL, left, new BooleanArrayOperand(values));
    }

    public Where ne(Integer value) {
      return new Where(Operator.NOT_EQUAL, left, new IntegerOperand(value));
    }

    public Where ne(Integer... values) {
      return new Where(Operator.NOT_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where ne(Number value) {
      return new Where(Operator.NOT_EQUAL, left, new NumberOperand(value.doubleValue()));
    }

    public Where ne(Number... values) {
      return new Where(Operator.NOT_EQUAL, left, new NumberArrayOperand(values));
    }

    public Where ne(Date value) {
      return new Where(Operator.NOT_EQUAL, left, new DateOperand(value));
    }

    public Where ne(Date... values) {
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

    public Where lt(Boolean value) {
      return new Where(Operator.LESS_THAN, left, new BooleanOperand(value));
    }

    public Where lt(Boolean... values) {
      return new Where(Operator.LESS_THAN, left, new BooleanArrayOperand(values));
    }

    public Where lt(Integer value) {
      return new Where(Operator.LESS_THAN, left, new IntegerOperand(value));
    }

    public Where lt(Integer... values) {
      return new Where(Operator.LESS_THAN, left, new IntegerArrayOperand(values));
    }

    public Where lt(Number value) {
      return new Where(Operator.LESS_THAN, left, new NumberOperand(value.doubleValue()));
    }

    public Where lt(Number... values) {
      return new Where(Operator.LESS_THAN, left, new NumberArrayOperand(values));
    }

    public Where lt(Date value) {
      return new Where(Operator.LESS_THAN, left, new DateOperand(value));
    }

    public Where lt(Date... values) {
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

    public Where lte(Boolean value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new BooleanOperand(value));
    }

    public Where lte(Boolean... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new BooleanArrayOperand(values));
    }

    public Where lte(Integer value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Where lte(Integer... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where lte(Number value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new NumberOperand(value.doubleValue()));
    }

    public Where lte(Number... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new NumberArrayOperand(values));
    }

    public Where lte(Date value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new DateOperand(value));
    }

    public Where lte(Date... values) {
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

    public Where gt(Boolean value) {
      return new Where(Operator.GREATER_THAN, left, new BooleanOperand(value));
    }

    public Where gt(Boolean... values) {
      return new Where(Operator.GREATER_THAN, left, new BooleanArrayOperand(values));
    }

    public Where gt(Integer value) {
      return new Where(Operator.GREATER_THAN, left, new IntegerOperand(value));
    }

    public Where gt(Integer... values) {
      return new Where(Operator.GREATER_THAN, left, new IntegerArrayOperand(values));
    }

    public Where gt(Number value) {
      return new Where(Operator.GREATER_THAN, left, new NumberOperand(value.doubleValue()));
    }

    public Where gt(Number... values) {
      return new Where(Operator.GREATER_THAN, left, new NumberArrayOperand(values));
    }

    public Where gt(Date value) {
      return new Where(Operator.GREATER_THAN, left, new DateOperand(value));
    }

    public Where gt(Date... values) {
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

    public Where gte(Boolean value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new BooleanOperand(value));
    }

    public Where gte(Boolean... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new BooleanArrayOperand(values));
    }

    public Where gte(Integer value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Where gte(Integer... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Where gte(Number value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new NumberOperand(value.doubleValue()));
    }

    public Where gte(Number... values) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new NumberArrayOperand(values));
    }

    public Where gte(Date value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new DateOperand(value));
    }

    public Where gte(Date... values) {
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

    public Where like(String... values) {
      return new Where(Operator.LIKE, left, new TextArrayOperand(values));
    }

    public Where like(Boolean value) {
      return new Where(Operator.LIKE, left, new BooleanOperand(value));
    }

    public Where like(Boolean... values) {
      return new Where(Operator.LIKE, left, new BooleanArrayOperand(values));
    }

    public Where like(Integer value) {
      return new Where(Operator.LIKE, left, new IntegerOperand(value));
    }

    public Where like(Integer... values) {
      return new Where(Operator.LIKE, left, new IntegerArrayOperand(values));
    }

    public Where like(Number value) {
      return new Where(Operator.LIKE, left, new NumberOperand(value.doubleValue()));
    }

    public Where like(Number... values) {
      return new Where(Operator.LIKE, left, new NumberArrayOperand(values));
    }

    public Where like(Date value) {
      return new Where(Operator.LIKE, left, new DateOperand(value));
    }

    public Where like(Date... values) {
      return new Where(Operator.LIKE, left, new DateArrayOperand(values));
    }

    public Where like(Object value) {
      return new Where(Operator.LIKE, left, fromObject(value));
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

    public Where containsAny(Integer... values) {
      return new Where(Operator.CONTAINS_ANY, left, new IntegerArrayOperand(values));
    }

    public Where containsAny(Number... values) {
      return new Where(Operator.CONTAINS_ANY, left, new NumberArrayOperand(values));
    }

    public Where containsAny(Date... values) {
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

    public Where containsAll(Integer... values) {
      return new Where(Operator.CONTAINS_ALL, left, new IntegerArrayOperand(values));
    }

    public Where containsAll(Number... values) {
      return new Where(Operator.CONTAINS_ALL, left, new NumberArrayOperand(values));
    }

    public Where containsAll(Date... values) {
      return new Where(Operator.CONTAINS_ALL, left, new DateArrayOperand(values));
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
    switch (operands.size()) {
      case 0:
        return;
      case 1: // no need for operator
        operands.get(0).appendTo(where);
        return;
      default:
        if (operator.equals(Operator.AND) || operator.equals(Operator.OR)) {
          operands.forEach(op -> {
            Filters.Builder nested = Filters.newBuilder();
            op.appendTo(nested);
            where.addFilters(nested);
          });
        } else {
          // Comparison operators: eq, gt, lt, like, etc.
          operands.forEach(op -> op.appendTo(where));
        }
    }
    operator.appendTo(where);
  }

  @SuppressWarnings("unchecked")
  static WhereOperand fromObject(Object value) {
    if (value instanceof String) {
      return new TextOperand((String) value);
    } else if (value instanceof Boolean) {
      return new BooleanOperand((Boolean) value);
    } else if (value instanceof Integer) {
      return new IntegerOperand((Integer) value);
    } else if (value instanceof Number) {
      return new NumberOperand((Number) value);
    } else if (value instanceof Date) {
      return new DateOperand((Date) value);
    } else if (value instanceof String[]) {
      return new TextArrayOperand((String[]) value);
    } else if (value instanceof Boolean[]) {
      return new BooleanArrayOperand((Boolean[]) value);
    } else if (value instanceof Integer[]) {
      return new IntegerArrayOperand((Integer[]) value);
    } else if (value instanceof Number[]) {
      return new NumberArrayOperand((Number[]) value);
    } else if (value instanceof Date[]) {
      return new DateArrayOperand((Date[]) value);
    } else if (value instanceof List) {
      if (((List<?>) value).isEmpty()) {
        throw new IllegalArgumentException(
            "Filter with non-reifiable type (List<T>) cannot be empty, use an array instead");
      }

      Object first = ((List<?>) value).get(0);
      if (first instanceof String) {
        return new TextArrayOperand((List<String>) value);
      } else if (first instanceof Boolean) {
        return new BooleanArrayOperand((List<Boolean>) value);
      } else if (first instanceof Integer) {
        return new IntegerArrayOperand((List<Integer>) value);
      } else if (first instanceof Number) {
        return new NumberArrayOperand((List<Number>) value);
      } else if (first instanceof Date) {
        return new DateArrayOperand((List<Date>) value);
      }
    }
    throw new IllegalArgumentException(
        "value must be either of String, Boolean, Date, Integer, Number, Array/List of these types");
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
    private final Boolean value;

    private BooleanOperand(Boolean value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueBoolean(value);
    }

    @Override
    public String toString() {
      return value.toString();
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
    private final Integer value;

    private IntegerOperand(Integer value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueInt(value);
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }

  private static class IntegerArrayOperand implements WhereOperand {
    private final List<Integer> values;

    private IntegerArrayOperand(List<Integer> values) {
      this.values = values;
    }

    @SafeVarargs
    private IntegerArrayOperand(Integer... values) {
      this(Arrays.asList(values));
    }

    private List<Long> toLongs() {
      return values.stream().map(Integer::longValue).toList();
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueIntArray(WeaviateProtoBase.IntArray.newBuilder().addAllValues(toLongs()));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class NumberOperand implements WhereOperand {
    private final Number value;

    private NumberOperand(Number value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueNumber(value.doubleValue());
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }

  private static class NumberArrayOperand implements WhereOperand {
    private final List<Number> values;

    private NumberArrayOperand(List<Number> values) {
      this.values = values;
    }

    @SafeVarargs
    private NumberArrayOperand(Number... values) {
      this(Arrays.asList(values));
    }

    private List<Double> toDoubles() {
      return values.stream().map(Number::doubleValue).toList();
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueNumberArray(WeaviateProtoBase.NumberArray.newBuilder().addAllValues(toDoubles()));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class DateOperand implements WhereOperand {
    private final Date value;

    private DateOperand(Date value) {
      this.value = value;
    }

    private static String format(Date date) {
      return DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder where) {
      where.setValueText(format(value));
    }

    @Override
    public String toString() {
      return format(value);
    }
  }

  private static class DateArrayOperand implements WhereOperand {
    private final List<Date> values;

    private DateArrayOperand(List<Date> values) {
      this.values = values;
    }

    @SafeVarargs
    private DateArrayOperand(Date... values) {
      this(Arrays.asList(values));
    }

    private List<String> formatted() {
      return values.stream().map(date -> DateOperand.format(date)).toList();
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
