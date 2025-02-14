package io.weaviate.client.v1.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.Filters;
import lombok.RequiredArgsConstructor;

public class Where implements Operand {

  @RequiredArgsConstructor
  public enum Operator {
    // Logical operators
    AND("And", Filters.Operator.OPERATOR_AND),
    OR("Or", Filters.Operator.OPERATOR_OR),

    // Comparison operators
    EQUAL("Equal", Filters.Operator.OPERATOR_EQUAL),
    NOT_EQUAL("NotEqual", Filters.Operator.OPERATOR_NOT_EQUAL),
    LESS_THAN("LessThen", Filters.Operator.OPERATOR_LESS_THAN),
    LESS_THAN_EQUAL("LessThenEqual", Filters.Operator.OPERATOR_LESS_THAN_EQUAL),
    GREATER_THAN("GreaterThen", Filters.Operator.OPERATOR_GREATER_THAN),
    GREATER_THAN_EQUAL("GreaterThenEqual", Filters.Operator.OPERATOR_GREATER_THAN_EQUAL),
    LIKE("Like", Filters.Operator.OPERATOR_LIKE),
    CONTAINS_ANY("ContainsAny", Filters.Operator.OPERATOR_LIKE),
    CONTAINS_ALL("ContainsAll", Filters.Operator.OPERATOR_CONTAINS_ALL),
    WITHIN_GEO_RANGE("WithinGeoRange", Filters.Operator.OPERATOR_WITHIN_GEO_RANGE);

    /** String representation for better debug logs. */
    private final String string;

    /** gRPC operator value . */
    private final Filters.Operator grpc;

    public void append(Filters.Builder where) {
      where.setOperator(grpc);
    }

    public String toString() {
      return string;
    }
  }

  private final Operator operator;
  private List<Operand> operands = new ArrayList<>();

  public boolean isEmpty() {
    // TODO: if operands not empty, we need to check that each operand is not empty
    // either. Guard against Where.and(Where.or(), Where.and()) situation.
    return operands.isEmpty();
  }

  @SafeVarargs
  private Where(Operator operator, Operand... operands) {
    this(operator, Arrays.asList(operands));
  }

  private Where(Operator operator, List<Operand> operands) {
    this.operator = operator;
    this.operands = operands;
  }

  // Logical operators return a complete operand.
  // --------------------------------------------
  public static Where and(Operand... operands) {
    return new Where(Operator.AND, operands);
  }

  public static Where and(Map<String, Object> filters, Operator operator) {
    return new Where(Operator.AND, fromMap(filters, operator));
  }

  public static Where or(Operand... operands) {
    return new Where(Operator.OR, operands);
  }

  public static Where or(Map<String, Object> filters, Operator operator) {
    return new Where(Operator.OR, fromMap(filters, operator));
  }

  public static List<Operand> fromMap(Map<String, Object> filters, Operator operator) {
    if (operator.equals(Operator.AND) || operator.equals(Operator.OR)) {
      // TODO: we will avoid this by not exposing AND/OR operators to the user.
      throw new IllegalArgumentException("AND/OR operators are not comparison operators");
    }
    return filters.entrySet().stream()
        .<Operand>map(entry -> new Where(
            operator,
            new Path(entry.getKey()),
            ComparisonBuilder.fromObject(entry.getValue())))
        .toList();
  }

  // Comparison operators return fluid builder.
  // ------------------------------------------

  public static ComparisonBuilder property(String property) {
    return new ComparisonBuilder(new Path(property));
  }

  public static ComparisonBuilder reference(String... path) {
    return new ComparisonBuilder(new Path(path));
  }

  public static class ComparisonBuilder {
    private Operand left;

    private ComparisonBuilder(Operand left) {
      this.left = left;
    }

    @SuppressWarnings("unchecked")
    static Operand fromObject(Object value) {
      if (value instanceof String) {
        return new $Text((String) value);
      } else if (value instanceof Boolean) {
        return new $Boolean((Boolean) value);
      } else if (value instanceof Integer) {
        return new $Integer((Integer) value);
      } else if (value instanceof Number) {
        return new $Number((Number) value);
      } else if (value instanceof Date) {
        return new $Date((Date) value);
      } else if (value instanceof List) {
        assert ((List<?>) value).isEmpty() : "list must not be empty";

        Object first = ((List<?>) value).get(0);
        if (first instanceof String) {
          return new $TextArray((List<String>) value);
        } else if (first instanceof Boolean) {
          return new $BooleanArray((List<Boolean>) value);
        } else if (first instanceof Integer) {
          return new $IntegerArray((List<Integer>) value);
        } else if (first instanceof Number) {
          return new $NumberArray((List<Number>) value);
        } else if (first instanceof Date) {
          return new $DateArray((List<Date>) value);
        }
      }
      throw new IllegalArgumentException("value must be either of String, Boolean, Date, Integer, Number, List");
    }

    // Equal
    // ------------------------------------------
    public Where eq(String value) {
      return new Where(Operator.EQUAL, left, new $Text(value));
    }

    public Where eq(String... values) {
      return new Where(Operator.EQUAL, left, new $TextArray(values));
    }

    public Where eq(Boolean value) {
      return new Where(Operator.EQUAL, left, new $Boolean(value));
    }

    public Where eq(Boolean... values) {
      return new Where(Operator.EQUAL, left, new $BooleanArray(values));
    }

    public Where eq(Integer value) {
      return new Where(Operator.EQUAL, left, new $Integer(value));
    }

    public Where eq(Integer... values) {
      return new Where(Operator.EQUAL, left, new $IntegerArray(values));
    }

    public Where eq(Number value) {
      return new Where(Operator.EQUAL, left, new $Number(value.doubleValue()));
    }

    public Where eq(Number... values) {
      return new Where(Operator.EQUAL, left, new $NumberArray(values));
    }

    public Where eq(Date value) {
      return new Where(Operator.EQUAL, left, new $Date(value));
    }

    public Where eq(Date... values) {
      return new Where(Operator.EQUAL, left, new $DateArray(values));
    }

    public Where eq(Object value) {
      return new Where(Operator.EQUAL, left, fromObject(value));
    }

    // NotEqual
    // ------------------------------------------
    public Where ne(String value) {
      return new Where(Operator.NOT_EQUAL, left, new $Text(value));
    }

    public Where ne(String... values) {
      return new Where(Operator.NOT_EQUAL, left, new $TextArray(values));
    }

    public Where ne(Boolean value) {
      return new Where(Operator.NOT_EQUAL, left, new $Boolean(value));
    }

    public Where ne(Boolean... values) {
      return new Where(Operator.NOT_EQUAL, left, new $BooleanArray(values));
    }

    public Where ne(Integer value) {
      return new Where(Operator.NOT_EQUAL, left, new $Integer(value));
    }

    public Where ne(Integer... values) {
      return new Where(Operator.NOT_EQUAL, left, new $IntegerArray(values));
    }

    public Where ne(Number value) {
      return new Where(Operator.NOT_EQUAL, left, new $Number(value.doubleValue()));
    }

    public Where ne(Number... values) {
      return new Where(Operator.NOT_EQUAL, left, new $NumberArray(values));
    }

    public Where ne(Date value) {
      return new Where(Operator.NOT_EQUAL, left, new $Date(value));
    }

    public Where ne(Date... values) {
      return new Where(Operator.NOT_EQUAL, left, new $DateArray(values));
    }

    // LessThan
    // ------------------------------------------
    public Where lt(String value) {
      return new Where(Operator.LESS_THAN, left, new $Text(value));
    }

    public Where lt(String... values) {
      return new Where(Operator.LESS_THAN, left, new $TextArray(values));
    }

    public Where lt(Boolean value) {
      return new Where(Operator.LESS_THAN, left, new $Boolean(value));
    }

    public Where lt(Boolean... values) {
      return new Where(Operator.LESS_THAN, left, new $BooleanArray(values));
    }

    public Where lt(Integer value) {
      return new Where(Operator.LESS_THAN, left, new $Integer(value));
    }

    public Where lt(Integer... values) {
      return new Where(Operator.LESS_THAN, left, new $IntegerArray(values));
    }

    public Where lt(Number value) {
      return new Where(Operator.LESS_THAN, left, new $Number(value.doubleValue()));
    }

    public Where lt(Number... values) {
      return new Where(Operator.LESS_THAN, left, new $NumberArray(values));
    }

    public Where lt(Date value) {
      return new Where(Operator.LESS_THAN, left, new $Date(value));
    }

    public Where lt(Date... values) {
      return new Where(Operator.LESS_THAN, left, new $DateArray(values));
    }

    // LessThanEqual
    // ------------------------------------------
    public Where lte(String value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $Text(value));
    }

    public Where lte(String... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $TextArray(values));
    }

    public Where lte(Boolean value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $Boolean(value));
    }

    public Where lte(Boolean... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $BooleanArray(values));
    }

    public Where lte(Integer value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $Integer(value));
    }

    public Where lte(Integer... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $IntegerArray(values));
    }

    public Where lte(Number value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $Number(value.doubleValue()));
    }

    public Where lte(Number... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $NumberArray(values));
    }

    public Where lte(Date value) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $Date(value));
    }

    public Where lte(Date... values) {
      return new Where(Operator.LESS_THAN_EQUAL, left, new $DateArray(values));
    }

    // GreaterThan
    // ------------------------------------------
    public Where gt(String value) {
      return new Where(Operator.GREATER_THAN, left, new $Text(value));
    }

    public Where gt(String... values) {
      return new Where(Operator.GREATER_THAN, left, new $TextArray(values));
    }

    public Where gt(Boolean value) {
      return new Where(Operator.GREATER_THAN, left, new $Boolean(value));
    }

    public Where gt(Boolean... values) {
      return new Where(Operator.GREATER_THAN, left, new $BooleanArray(values));
    }

    public Where gt(Integer value) {
      return new Where(Operator.GREATER_THAN, left, new $Integer(value));
    }

    public Where gt(Integer... values) {
      return new Where(Operator.GREATER_THAN, left, new $IntegerArray(values));
    }

    public Where gt(Number value) {
      return new Where(Operator.GREATER_THAN, left, new $Number(value.doubleValue()));
    }

    public Where gt(Number... values) {
      return new Where(Operator.GREATER_THAN, left, new $NumberArray(values));
    }

    public Where gt(Date value) {
      return new Where(Operator.GREATER_THAN, left, new $Date(value));
    }

    public Where gt(Date... values) {
      return new Where(Operator.GREATER_THAN, left, new $DateArray(values));
    }

    // GreaterThanEqual
    // ------------------------------------------
    public Where gte(String value) {
      return new Where(Operator.GREATER_THAN_EQUAL, left, new $Text(value));
    }

    public Where gte(String... values) {
      return new Where(Operator.GREATER_THAN, left, new $TextArray(values));
    }

    public Where gte(Boolean value) {
      return new Where(Operator.GREATER_THAN, left, new $Boolean(value));
    }

    public Where gte(Boolean... values) {
      return new Where(Operator.GREATER_THAN, left, new $BooleanArray(values));
    }

    public Where gte(Integer value) {
      return new Where(Operator.GREATER_THAN, left, new $Integer(value));
    }

    public Where gte(Integer... values) {
      return new Where(Operator.GREATER_THAN, left, new $IntegerArray(values));
    }

    public Where gte(Number value) {
      return new Where(Operator.GREATER_THAN, left, new $Number(value.doubleValue()));
    }

    public Where gte(Number... values) {
      return new Where(Operator.GREATER_THAN, left, new $NumberArray(values));
    }

    public Where gte(Date value) {
      return new Where(Operator.GREATER_THAN, left, new $Date(value));
    }

    public Where gte(Date... values) {
      return new Where(Operator.GREATER_THAN, left, new $DateArray(values));
    }

    // Like
    // ------------------------------------------
    public Where like(String value) {
      return new Where(Operator.LIKE, left, new $Text(value));
    }

    public Where like(String... values) {
      return new Where(Operator.LIKE, left, new $TextArray(values));
    }

    public Where like(Boolean value) {
      return new Where(Operator.LIKE, left, new $Boolean(value));
    }

    public Where like(Boolean... values) {
      return new Where(Operator.LIKE, left, new $BooleanArray(values));
    }

    public Where like(Integer value) {
      return new Where(Operator.LIKE, left, new $Integer(value));
    }

    public Where like(Integer... values) {
      return new Where(Operator.LIKE, left, new $IntegerArray(values));
    }

    public Where like(Number value) {
      return new Where(Operator.LIKE, left, new $Number(value.doubleValue()));
    }

    public Where like(Number... values) {
      return new Where(Operator.LIKE, left, new $NumberArray(values));
    }

    public Where like(Date value) {
      return new Where(Operator.LIKE, left, new $Date(value));
    }

    public Where like(Date... values) {
      return new Where(Operator.LIKE, left, new $DateArray(values));
    }

    // ContainsAny
    // ------------------------------------------
    public Where containsAny(String value) {
      return new Where(Operator.CONTAINS_ANY, left, new $Text(value));
    }

    public Where containsAny(String... values) {
      return new Where(Operator.CONTAINS_ANY, left, new $TextArray(values));
    }

    public Where containsAny(Boolean... values) {
      return new Where(Operator.CONTAINS_ANY, left, new $BooleanArray(values));
    }

    public Where containsAny(Integer... values) {
      return new Where(Operator.CONTAINS_ANY, left, new $IntegerArray(values));
    }

    public Where containsAny(Number... values) {
      return new Where(Operator.CONTAINS_ANY, left, new $NumberArray(values));
    }

    public Where containsAny(Date... values) {
      return new Where(Operator.CONTAINS_ANY, left, new $DateArray(values));
    }

    // ContainsAll
    // ------------------------------------------
    public Where containsAll(String value) {
      return new Where(Operator.CONTAINS_ALL, left, new $Text(value));
    }

    public Where containsAll(String... values) {
      return new Where(Operator.CONTAINS_ALL, left, new $TextArray(values));
    }

    public Where containsAll(Boolean... values) {
      return new Where(Operator.CONTAINS_ALL, left, new $BooleanArray(values));
    }

    public Where containsAll(Integer... values) {
      return new Where(Operator.CONTAINS_ALL, left, new $IntegerArray(values));
    }

    public Where containsAll(Number... values) {
      return new Where(Operator.CONTAINS_ALL, left, new $NumberArray(values));
    }

    public Where containsAll(Date... values) {
      return new Where(Operator.CONTAINS_ALL, left, new $DateArray(values));
    }

    // WithinGeoRange
    // ------------------------------------------
    public Where withinGeoRange(float lat, float lon, float maxDistance) {
      return new Where(Operator.WITHIN_GEO_RANGE, left, new $GeoRange(lat, lon, maxDistance));
    }

  }

  @Override
  public void append(Filters.Builder where) {
    switch (operands.size()) {
      case 0:
        return;
      case 1: // no need for operator
        operands.get(0).append(where);
        return;
      default:
        if (operator.equals(Operator.AND) || operator.equals(Operator.OR)) {
          operands.forEach(op -> {
            Filters.Builder nested = Filters.newBuilder();
            op.append(nested);
            where.addFilters(nested);
          });
        } else {
          // Comparison operators: eq, gt, lt, like, etc.
          operands.forEach(op -> op.append(where));
        }
    }
    operator.append(where);
  }

  private static class Path implements Operand {
    List<String> path = new ArrayList<>();

    @SafeVarargs
    private Path(String... property) {
      this.path = Arrays.asList(property);
    }

    @Override
    public void append(Filters.Builder where) {
      // Deprecated, but the current proto doesn't have 'path'.
      if (!path.isEmpty()) {
        where.addOn(path.get(0));
      }
      // FIXME: no way to reference objects rn?
    }
  }

  @RequiredArgsConstructor
  private static class $Text implements Operand {
    private final String value;

    @Override
    public void append(Filters.Builder where) {
      where.setValueText(value);
    }
  }

  @RequiredArgsConstructor
  private static class $TextArray implements Operand {
    private final List<String> value;

    @SafeVarargs
    private $TextArray(String... values) {
      this.value = Arrays.asList(values);
    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(value));
    }
  }

  @RequiredArgsConstructor
  private static class $Boolean implements Operand {
    private final Boolean value;

    @Override
    public void append(Filters.Builder where) {
      where.setValueBoolean(value);
    }
  }

  @RequiredArgsConstructor
  private static class $BooleanArray implements Operand {
    private final List<Boolean> value;

    @SafeVarargs
    private $BooleanArray(Boolean... values) {
      this.value = Arrays.asList(values);
      ;
    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueBooleanArray(WeaviateProtoBase.BooleanArray.newBuilder().addAllValues(value));
    }
  }

  @RequiredArgsConstructor
  private static class $Integer implements Operand {
    private final Integer value;

    @Override
    public void append(Filters.Builder where) {
      where.setValueInt(value);
    }
  }

  @RequiredArgsConstructor
  private static class $IntegerArray implements Operand {
    private final List<Integer> value;

    @SafeVarargs
    private $IntegerArray(Integer... values) {
      this.value = Arrays.asList(values);
      ;
    }

    private List<Long> toLongs() {
      return value.stream().map(Integer::longValue).toList();
    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueIntArray(WeaviateProtoBase.IntArray.newBuilder().addAllValues(toLongs()));
    }
  }

  @RequiredArgsConstructor
  private static class $Number implements Operand {
    private final Number value;

    @Override
    public void append(Filters.Builder where) {
      where.setValueNumber(value.doubleValue());
    }
  }

  @RequiredArgsConstructor
  private static class $NumberArray implements Operand {
    private final List<Number> value;

    @SafeVarargs
    private $NumberArray(Number... values) {
      this.value = Arrays.asList(values);
    }

    private List<Double> toDoubles() {
      return value.stream().map(Number::doubleValue).toList();
    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueNumberArray(WeaviateProtoBase.NumberArray.newBuilder().addAllValues(toDoubles()));
    }
  }

  @RequiredArgsConstructor
  private static class $Date implements Operand {
    private final Date value;

    private static String format(Date date) {
      return DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueText(format(value));
    }
  }

  @RequiredArgsConstructor
  private static class $DateArray implements Operand {
    private final List<Date> value;

    @SafeVarargs
    private $DateArray(Date... values) {
      this.value = Arrays.asList(values);
      ;
    }

    private List<String> formatted() {
      return value.stream().map(date -> $Date.format(date)).toList();

    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(formatted()));
    }
  }

  @RequiredArgsConstructor
  private static class $GeoRange implements Operand {
    private final Float lat;
    private final Float lon;
    private final Float distance;

    @Override
    public void append(Filters.Builder where) {
      where.setValueGeo(WeaviateProtoBase.GeoCoordinatesFilter.newBuilder()
          .setLatitude(lat).setLongitude(lon).setDistance(distance));
    }
  }
}
