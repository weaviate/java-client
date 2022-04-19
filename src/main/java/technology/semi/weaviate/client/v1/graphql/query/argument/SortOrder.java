package technology.semi.weaviate.client.v1.graphql.query.argument;

public enum SortOrder {
  asc("asc"),
  desc("desc");

  private final String order;

  SortOrder(String order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return order;
  }
}
