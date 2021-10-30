package technology.semi.weaviate.client.v1.graphql.query.argument;

public enum GroupType {
  merge("merge"),
  closest("closest");

  private final String type;

  GroupType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }
}
