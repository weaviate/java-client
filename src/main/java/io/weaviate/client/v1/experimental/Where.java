package io.weaviate.client.v1.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.Filters;
import lombok.RequiredArgsConstructor;

public class Where implements Operand {
  // Logical operators
  private static final String AND = "And";
  private static final String OR = "Or";

  // Comparison operators
  private static final String EQUAL = "Equals";
  private static final String LESS_THAN = "LessThan";
  private static final String GREATER_THAN = "GreaterThan";

  private final String operator;
  private List<Operand> operands = new ArrayList<>();

  @SafeVarargs
  private Where(String operator, Operand... operands) {
    this.operator = operator;
    this.operands = Arrays.asList(operands);
  }

  // Logical operators return a complete operand.
  // --------------------------------------------
  public static Where and(Operand... operands) {
    return new Where(AND, operands);
  }

  public static Where or(Operand... operands) {
    return new Where(OR, operands);
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

    public Where eq(String value) {
      return new Where(EQUAL, left, new Text(value));
    }

    public Where eq(String... value) {
      return new Where(EQUAL, left, new TextArray(value));
    }

    public Where lt(String value) {
      return new Where(LESS_THAN, left, new Text(value));
    }

    public Where lt(String... value) {
      return new Where(LESS_THAN, left, new TextArray(value));
    }

    public Where gt(String value) {
      return new Where(GREATER_THAN, left, new Text(value));
    }

    public Where gt(String... value) {
      return new Where(GREATER_THAN, left, new TextArray(value));
    }

    // TODO: there need to be overloaded operators for all possible combinations.
    // Verbose? Yes, but that's the way of Java. Plus it gives super nice syntax.
  }

  @Override
  public void append(Filters.Builder where) {
    switch (operands.size()) {
      case 0:
        return;
      case 1: // no need for operator
        operands.getFirst().append(where);
        return;
    }

    this.operands.forEach(op -> op.append(where));
    switch (operator) {
      case AND:
        where.setOperator(Filters.Operator.OPERATOR_AND);
        break;
      case OR:
        where.setOperator(Filters.Operator.OPERATOR_OR);
        break;
      case EQUAL:
        where.setOperator(Filters.Operator.OPERATOR_EQUAL);
        break;
      case GREATER_THAN:
        where.setOperator(Filters.Operator.OPERATOR_GREATER_THAN);
        break;
      case LESS_THAN:
        where.setOperator(Filters.Operator.OPERATOR_LESS_THAN);
        break;
    }
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
        where.addOn(path.getFirst());
      }
      // FIXME: no way to reference objects rn?
    }
  }

  @RequiredArgsConstructor
  private static class Text implements Operand {
    private final String value;

    @Override
    public void append(Filters.Builder where) {
      where.setValueText(value);
    }
  }

  private static class TextArray implements Operand {
    private List<String> value;

    @SafeVarargs
    private TextArray(String... value) {
      this.value = Arrays.asList(value);
    }

    @Override
    public void append(Filters.Builder where) {
      where.setValueTextArray(WeaviateProtoBase.TextArray.newBuilder().addAllValues(value).build());
    }
  }
}
