package io.weaviate.client6.v1.api.collections.query;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase.Filters;

public class Filter implements FilterOperand {

  private enum Operator {
    // Logical operators
    AND("And", WeaviateProtoBase.Filters.Operator.OPERATOR_AND),
    OR("Or", WeaviateProtoBase.Filters.Operator.OPERATOR_OR),
    NOT("Noe", WeaviateProtoBase.Filters.Operator.OPERATOR_NOT),

    // Comparison operators
    EQUAL("Equal", WeaviateProtoBase.Filters.Operator.OPERATOR_EQUAL),
    NOT_EQUAL("NotEqual", WeaviateProtoBase.Filters.Operator.OPERATOR_NOT_EQUAL),
    LESS_THAN("LessThan", WeaviateProtoBase.Filters.Operator.OPERATOR_LESS_THAN),
    LESS_THAN_EQUAL("LessThenEqual", WeaviateProtoBase.Filters.Operator.OPERATOR_LESS_THAN_EQUAL),
    GREATER_THAN("GreaterThan", WeaviateProtoBase.Filters.Operator.OPERATOR_GREATER_THAN),
    GREATER_THAN_EQUAL("GreaterThanEqual", WeaviateProtoBase.Filters.Operator.OPERATOR_GREATER_THAN_EQUAL),
    IS_NULL("IsNull", WeaviateProtoBase.Filters.Operator.OPERATOR_IS_NULL),
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

    public void appendTo(Filters.Builder filter) {
      filter.setOperator(grpcValue);
    }

    @Override
    public String toString() {
      return stringValue;
    }
  }

  private final Operator operator;
  private final List<FilterOperand> operands;

  @SafeVarargs
  private Filter(Operator operator, FilterOperand... operands) {
    this(operator, Arrays.asList(operands));
  }

  private Filter(Operator operator, List<FilterOperand> operands) {
    this.operator = operator;
    this.operands = operands;
  }

  @Override
  public boolean isEmpty() {
    // Guard against Filter.and(Filter.or(), Filter.and(), Filter.not()) situation.
    return operands.isEmpty()
        || operands.stream().allMatch(operator -> operator == null | operator.isEmpty());
  }

  @Override
  public String toString() {
    if (operator == Operator.NOT) {
      return "%s %s".formatted(operator, operands.get(0));
    }
    var operandStrings = operands.stream().map(Object::toString).toList();
    return "Filter(" + String.join(" " + operator.toString() + " ", operandStrings) + ")";
  }

  // Logical operators return a complete operand.
  // --------------------------------------------------------------------------
  public static Filter and(final FilterOperand... operands) {
    return new Filter(Operator.AND, operands);
  }

  public static Filter and(final List<FilterOperand> operands) {
    return new Filter(Operator.AND, operands);
  }

  public static Filter or(final FilterOperand... operands) {
    return new Filter(Operator.OR, operands);
  }

  public static Filter or(final List<FilterOperand> operands) {
    return new Filter(Operator.OR, operands);
  }

  public static Filter not(final FilterOperand operand) {
    return new Filter(Operator.NOT, operand);
  }

  /** Negate this expression. */
  public Filter not() {
    return not(this);
  }

  // Comparison operators return fluid builder.
  // --------------------------------------------------------------------------

  /** Filter by object UUID. */
  public static UuidProperty uuid() {
    return new UuidProperty();
  }

  /** Filter by object creation time. */
  public static DateProperty createdAt() {
    return new DateProperty(BaseQueryOptions.CREATION_TIME_PROPERTY);
  }

  /** Filter by object last update time. */
  public static DateProperty lastUpdatedAt() {
    return new DateProperty(BaseQueryOptions.LAST_UPDATE_TIME_PROPERTY);
  }

  /** Filter by object property. */
  public static FilterBuilder property(String property) {
    return new FilterBuilder(new PathOperand(property));
  }

  /** Filter by a property of the referenced object. */
  public static FilterBuilder reference(String... path) {
    return new FilterBuilder(new PathOperand(path));
  }

  public static class FilterBuilder {
    private final FilterOperand left;

    private FilterBuilder(FilterOperand left) {
      this.left = left;
    }

    // Equal
    // ------------------------------------------------------------------------
    public Filter eq(String value) {
      return new Filter(Operator.EQUAL, left, new TextOperand(value));
    }

    public Filter eq(String... values) {
      return new Filter(Operator.EQUAL, left, new TextArrayOperand(values));
    }

    public Filter eq(boolean value) {
      return new Filter(Operator.EQUAL, left, new BooleanOperand(value));
    }

    public Filter eq(Boolean... values) {
      return new Filter(Operator.EQUAL, left, new BooleanArrayOperand(values));
    }

    public Filter eq(long value) {
      return new Filter(Operator.EQUAL, left, new IntegerOperand(value));
    }

    public Filter eq(int value) {
      return new Filter(Operator.EQUAL, left, new IntegerOperand(value));
    }

    public Filter eq(Long... values) {
      return new Filter(Operator.EQUAL, left, new IntegerArrayOperand(values));
    }

    public Filter eq(double value) {
      return new Filter(Operator.EQUAL, left, new NumberOperand(value));
    }

    public Filter eq(float value) {
      return new Filter(Operator.EQUAL, left, new NumberOperand(value));
    }

    public Filter eq(Double... values) {
      return new Filter(Operator.EQUAL, left, new NumberArrayOperand(values));
    }

    public Filter eq(OffsetDateTime value) {
      return new Filter(Operator.EQUAL, left, new DateOperand(value));
    }

    public Filter eq(OffsetDateTime... values) {
      return new Filter(Operator.EQUAL, left, new DateArrayOperand(values));
    }

    public Filter eq(Object value) {
      return new Filter(Operator.EQUAL, left, fromObject(value));
    }

    // NotEqual
    // ------------------------------------------------------------------------
    public Filter ne(String value) {
      return new Filter(Operator.NOT_EQUAL, left, new TextOperand(value));
    }

    public Filter ne(String... values) {
      return new Filter(Operator.NOT_EQUAL, left, new TextArrayOperand(values));
    }

    public Filter ne(boolean value) {
      return new Filter(Operator.NOT_EQUAL, left, new BooleanOperand(value));
    }

    public Filter ne(Boolean... values) {
      return new Filter(Operator.NOT_EQUAL, left, new BooleanArrayOperand(values));
    }

    public Filter ne(long value) {
      return new Filter(Operator.NOT_EQUAL, left, new IntegerOperand(value));
    }

    public Filter ne(int value) {
      return new Filter(Operator.NOT_EQUAL, left, new IntegerOperand(value));
    }

    public Filter ne(Long... values) {
      return new Filter(Operator.NOT_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Filter ne(double value) {
      return new Filter(Operator.NOT_EQUAL, left, new NumberOperand(value));
    }

    public Filter ne(float value) {
      return new Filter(Operator.NOT_EQUAL, left, new NumberOperand(value));
    }

    public Filter ne(Double... values) {
      return new Filter(Operator.NOT_EQUAL, left, new NumberArrayOperand(values));
    }

    public Filter ne(OffsetDateTime value) {
      return new Filter(Operator.NOT_EQUAL, left, new DateOperand(value));
    }

    public Filter ne(OffsetDateTime... values) {
      return new Filter(Operator.NOT_EQUAL, left, new DateArrayOperand(values));
    }

    public Filter ne(Object value) {
      return new Filter(Operator.NOT_EQUAL, left, fromObject(value));
    }

    // LessThan
    // ------------------------------------------------------------------------
    public Filter lt(String value) {
      return new Filter(Operator.LESS_THAN, left, new TextOperand(value));
    }

    public Filter lt(String... values) {
      return new Filter(Operator.LESS_THAN, left, new TextArrayOperand(values));
    }

    public Filter lt(long value) {
      return new Filter(Operator.LESS_THAN, left, new IntegerOperand(value));
    }

    public Filter lt(int value) {
      return new Filter(Operator.LESS_THAN, left, new IntegerOperand(value));
    }

    public Filter lt(Long... values) {
      return new Filter(Operator.LESS_THAN, left, new IntegerArrayOperand(values));
    }

    public Filter lt(double value) {
      return new Filter(Operator.LESS_THAN, left, new NumberOperand(value));
    }

    public Filter lt(float value) {
      return new Filter(Operator.LESS_THAN, left, new NumberOperand(value));
    }

    public Filter lt(Double... values) {
      return new Filter(Operator.LESS_THAN, left, new NumberArrayOperand(values));
    }

    public Filter lt(OffsetDateTime value) {
      return new Filter(Operator.LESS_THAN, left, new DateOperand(value));
    }

    public Filter lt(OffsetDateTime... values) {
      return new Filter(Operator.LESS_THAN, left, new DateArrayOperand(values));
    }

    public Filter lt(Object value) {
      return new Filter(Operator.LESS_THAN, left, fromObject(value));
    }

    // LessThanEqual
    // ------------------------------------------------------------------------
    public Filter lte(String value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new TextOperand(value));
    }

    public Filter lte(String... values) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new TextArrayOperand(values));
    }

    public Filter lte(long value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Filter lte(int value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Filter lte(Long... values) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Filter lte(double value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Filter lte(float value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Filter lte(Double... values) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new NumberArrayOperand(values));
    }

    public Filter lte(OffsetDateTime value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new DateOperand(value));
    }

    public Filter lte(OffsetDateTime... values) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, new DateArrayOperand(values));
    }

    public Filter lte(Object value) {
      return new Filter(Operator.LESS_THAN_EQUAL, left, fromObject(value));
    }

    // GreaterThan
    // ------------------------------------------------------------------------
    public Filter gt(String value) {
      return new Filter(Operator.GREATER_THAN, left, new TextOperand(value));
    }

    public Filter gt(String... values) {
      return new Filter(Operator.GREATER_THAN, left, new TextArrayOperand(values));
    }

    public Filter gt(long value) {
      return new Filter(Operator.GREATER_THAN, left, new IntegerOperand(value));
    }

    public Filter gt(int value) {
      return new Filter(Operator.GREATER_THAN, left, new IntegerOperand(value));
    }

    public Filter gt(Long... values) {
      return new Filter(Operator.GREATER_THAN, left, new IntegerArrayOperand(values));
    }

    public Filter gt(double value) {
      return new Filter(Operator.GREATER_THAN, left, new NumberOperand(value));
    }

    public Filter gt(float value) {
      return new Filter(Operator.GREATER_THAN, left, new NumberOperand(value));
    }

    public Filter gt(Double... values) {
      return new Filter(Operator.GREATER_THAN, left, new NumberArrayOperand(values));
    }

    public Filter gt(OffsetDateTime value) {
      return new Filter(Operator.GREATER_THAN, left, new DateOperand(value));
    }

    public Filter gt(OffsetDateTime... values) {
      return new Filter(Operator.GREATER_THAN, left, new DateArrayOperand(values));
    }

    public Filter gt(Object value) {
      return new Filter(Operator.GREATER_THAN, left, fromObject(value));
    }

    // GreaterThanEqual
    // ------------------------------------------------------------------------
    public Filter gte(String value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new TextOperand(value));
    }

    public Filter gte(String... values) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new TextArrayOperand(values));
    }

    public Filter gte(long value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Filter gte(int value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new IntegerOperand(value));
    }

    public Filter gte(Long... values) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new IntegerArrayOperand(values));
    }

    public Filter gte(double value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Filter gte(float value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new NumberOperand(value));
    }

    public Filter gte(Double... values) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new NumberArrayOperand(values));
    }

    public Filter gte(OffsetDateTime value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new DateOperand(value));
    }

    public Filter gte(OffsetDateTime... values) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, new DateArrayOperand(values));
    }

    public Filter gte(Object value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, left, fromObject(value));
    }

    // IsNull
    // ------------------------------------------------------------------------
    public Filter isNull() {
      return isNull(true);
    }

    public Filter isNotNull() {
      return isNull(false);
    }

    public Filter isNull(boolean isNull) {
      return new Filter(Operator.IS_NULL, left, new BooleanOperand(isNull));
    }

    // Like
    // ------------------------------------------------------------------------
    public Filter like(String value) {
      return new Filter(Operator.LIKE, left, new TextOperand(value));
    }

    // ContainsAny
    // ------------------------------------------------------------------------
    public Filter containsAny(String value) {
      return new Filter(Operator.CONTAINS_ANY, left, new TextOperand(value));
    }

    public Filter containsAny(String... values) {
      return new Filter(Operator.CONTAINS_ANY, left, new TextArrayOperand(values));
    }

    public Filter containsAny(List<String> values) {
      return new Filter(Operator.CONTAINS_ANY, left, new TextArrayOperand(values));
    }

    public Filter containsAny(Boolean... values) {
      return new Filter(Operator.CONTAINS_ANY, left, new BooleanArrayOperand(values));
    }

    public Filter containsAny(Long... values) {
      return new Filter(Operator.CONTAINS_ANY, left, new IntegerArrayOperand(values));
    }

    public Filter containsAny(Double... values) {
      return new Filter(Operator.CONTAINS_ANY, left, new NumberArrayOperand(values));
    }

    public Filter containsAny(OffsetDateTime... values) {
      return new Filter(Operator.CONTAINS_ANY, left, new DateArrayOperand(values));
    }

    // ContainsAll
    // ------------------------------------------------------------------------
    public Filter containsAll(String value) {
      return new Filter(Operator.CONTAINS_ALL, left, new TextOperand(value));
    }

    public Filter containsAll(String... values) {
      return new Filter(Operator.CONTAINS_ALL, left, new TextArrayOperand(values));
    }

    public Filter containsAll(List<String> values) {
      return new Filter(Operator.CONTAINS_ALL, left, new TextArrayOperand(values));
    }

    public Filter containsAll(Boolean... values) {
      return new Filter(Operator.CONTAINS_ALL, left, new BooleanArrayOperand(values));
    }

    public Filter containsAll(Long... values) {
      return new Filter(Operator.CONTAINS_ALL, left, new IntegerArrayOperand(values));
    }

    public Filter containsAll(Double... values) {
      return new Filter(Operator.CONTAINS_ALL, left, new NumberArrayOperand(values));
    }

    public Filter containsAll(OffsetDateTime... values) {
      return new Filter(Operator.CONTAINS_ALL, left, new DateArrayOperand(values));
    }

    // ContainsNone
    // ------------------------------------------------------------------------
    public Filter containsNone(String value) {
      return new Filter(Operator.CONTAINS_NONE, left, new TextOperand(value));
    }

    public Filter containsNone(String... values) {
      return new Filter(Operator.CONTAINS_NONE, left, new TextArrayOperand(values));
    }

    public Filter containsNone(List<String> values) {
      return new Filter(Operator.CONTAINS_NONE, left, new TextArrayOperand(values));
    }

    public Filter containsNone(Boolean... values) {
      return new Filter(Operator.CONTAINS_NONE, left, new BooleanArrayOperand(values));
    }

    public Filter containsNone(Long... values) {
      return new Filter(Operator.CONTAINS_NONE, left, new IntegerArrayOperand(values));
    }

    public Filter containsNone(Double... values) {
      return new Filter(Operator.CONTAINS_NONE, left, new NumberArrayOperand(values));
    }

    public Filter containsNone(OffsetDateTime... values) {
      return new Filter(Operator.CONTAINS_NONE, left, new DateArrayOperand(values));
    }

    // WithinGeoRange
    // ------------------------------------------------------------------------
    public Filter withinGeoRange(float lat, float lon, float maxDistance) {
      return new Filter(Operator.WITHIN_GEO_RANGE, left, new GeoRangeOperand(lat, lon, maxDistance));
    }
  }

  @Override
  public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
    if (isEmpty()) {
      return;
    }

    operator.appendTo(filter);

    if (operator == Operator.AND || operator == Operator.OR || operator == Operator.NOT) {
      operands.forEach(op -> {
        var nested = Filters.newBuilder();
        op.appendTo(nested);
        filter.addFilters(nested);
      });
    } else {
      // Comparison operators: eq, gt, lt, like, etc.
      operands.forEach(op -> op.appendTo(filter));
    }
  }

  @SuppressWarnings("unchecked")
  static FilterOperand fromObject(Object value) {
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

  private static class PathOperand implements FilterOperand {
    private final List<String> path;

    private PathOperand(List<String> path) {
      this.path = path;
    }

    private PathOperand(String... path) {
      this(Arrays.asList(path));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      // "on" is deprecated, but the current proto doesn't have "path".
      if (!path.isEmpty()) {
        filter.addOn(path.get(0));
      }
      // FIXME: no way to reference objects rn?
    }

    @Override
    public String toString() {
      return String.join("::", path);
    }
  }

  public static class UuidProperty extends PathOperand {
    private UuidProperty() {
      super(BaseQueryOptions.ID_PROPERTY);
    }

    public Filter eq(String value) {
      return new Filter(Operator.EQUAL, this, new TextOperand(value));
    }

    public Filter ne(String value) {
      return new Filter(Operator.NOT_EQUAL, this, new TextOperand(value));
    }

    public Filter gt(String value) {
      return new Filter(Operator.GREATER_THAN, this, new TextOperand(value));
    }

    public Filter gte(String value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, this, new TextOperand(value));
    }

    public Filter lt(String value) {
      return new Filter(Operator.LESS_THAN, this, new TextOperand(value));
    }

    public Filter lte(String value) {
      return new Filter(Operator.LESS_THAN_EQUAL, this, new TextOperand(value));
    }

    public Filter containsAny(String... values) {
      return new Filter(Operator.CONTAINS_ANY, this, new TextArrayOperand(values));
    }

    public Filter containsNone(String... values) {
      return new Filter(Operator.CONTAINS_NONE, this, new TextArrayOperand(values));
    }
  }

  public static class DateProperty extends PathOperand {
    private DateProperty(String propertyName) {
      super(propertyName);
    }

    public Filter eq(OffsetDateTime value) {
      return new Filter(Operator.EQUAL, this, new DateOperand(value));
    }

    public Filter ne(OffsetDateTime value) {
      return new Filter(Operator.NOT_EQUAL, this, new DateOperand(value));
    }

    public Filter gt(OffsetDateTime value) {
      return new Filter(Operator.GREATER_THAN, this, new DateOperand(value));
    }

    public Filter gte(OffsetDateTime value) {
      return new Filter(Operator.GREATER_THAN_EQUAL, this, new DateOperand(value));
    }

    public Filter lt(OffsetDateTime value) {
      return new Filter(Operator.LESS_THAN, this, new DateOperand(value));
    }

    public Filter lte(OffsetDateTime value) {
      return new Filter(Operator.LESS_THAN_EQUAL, this, new DateOperand(value));
    }

    public Filter containsAny(OffsetDateTime... values) {
      return new Filter(Operator.CONTAINS_ANY, this, new DateArrayOperand(values));
    }

    public Filter containsNone(OffsetDateTime... values) {
      return new Filter(Operator.CONTAINS_NONE, this, new DateArrayOperand(values));
    }
  }

  private static class TextOperand implements FilterOperand {
    private final String value;

    private TextOperand(String value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueText(value);
    }

    @Override
    public String toString() {
      return value;
    }
  }

  private static class TextArrayOperand implements FilterOperand {
    private final List<String> values;

    private TextArrayOperand(List<String> values) {
      this.values = values;
    }

    @SafeVarargs
    private TextArrayOperand(String... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class BooleanOperand implements FilterOperand {
    private final boolean value;

    private BooleanOperand(boolean value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueBoolean(value);
    }

    @Override
    public String toString() {
      return Boolean.toString(value);
    }
  }

  private static class BooleanArrayOperand implements FilterOperand {
    private final List<Boolean> values;

    private BooleanArrayOperand(List<Boolean> values) {
      this.values = values;
    }

    @SafeVarargs
    private BooleanArrayOperand(Boolean... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueBooleanArray(WeaviateProtoBase.BooleanArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class IntegerOperand implements FilterOperand {
    private final long value;

    private IntegerOperand(long value) {
      this.value = value;
    }

    private IntegerOperand(int value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueInt(value);
    }

    @Override
    public String toString() {
      return Long.toString(value);
    }
  }

  private static class IntegerArrayOperand implements FilterOperand {
    private final List<Long> values;

    private IntegerArrayOperand(List<Long> values) {
      this.values = values;
    }

    @SafeVarargs
    private IntegerArrayOperand(Long... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueIntArray(WeaviateProtoBase.IntArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class NumberOperand implements FilterOperand {
    private final double value;

    private NumberOperand(double value) {
      this.value = value;
    }

    private NumberOperand(float value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueNumber(value);
    }

    @Override
    public String toString() {
      return Double.toString(value);
    }
  }

  private static class NumberArrayOperand implements FilterOperand {
    private final List<Double> values;

    private NumberArrayOperand(List<Double> values) {
      this.values = values;
    }

    @SafeVarargs
    private NumberArrayOperand(Double... values) {
      this(Arrays.asList(values));
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueNumberArray(WeaviateProtoBase.NumberArray.newBuilder().addAllValues(values));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class DateOperand implements FilterOperand {
    private final OffsetDateTime value;

    private DateOperand(OffsetDateTime value) {
      this.value = value;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueText(value.toString());
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }

  private static class DateArrayOperand implements FilterOperand {
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
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(formatted()));
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }

  private static class GeoRangeOperand implements FilterOperand {
    private final float lat;
    private final float lon;
    private final float distance;

    private GeoRangeOperand(float lat, float lon, float distance) {
      this.lat = lat;
      this.lon = lon;
      this.distance = distance;
    }

    @Override
    public void appendTo(WeaviateProtoBase.Filters.Builder filter) {
      filter.setValueGeo(WeaviateProtoBase.GeoCoordinatesFilter.newBuilder()
          .setLatitude(lat).setLongitude(lon).setDistance(distance));
    }

    @Override
    public String toString() {
      return "(lat=%d, lon=%d, distance=%d)".formatted(lat, lon, distance);
    }
  }
}
