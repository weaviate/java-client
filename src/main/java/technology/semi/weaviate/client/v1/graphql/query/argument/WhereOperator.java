package technology.semi.weaviate.client.v1.graphql.query.argument;

public enum WhereOperator {
  And("And"),
  Like("Like"),
  Or("Or"),
  Equal("Equal"),
  Not("Not"),
  NotEqual("NotEqual"),
  GreaterThan("GreaterThan"),
  GreaterThanEqual("GreaterThanEqual"),
  LessThan("LessThan"),
  LessThanEqual("LessThanEqual"),
  WithinGeoRange("WithinGeoRange");

  private final String operator;

  WhereOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public String toString() {
    return operator;
  }
}
