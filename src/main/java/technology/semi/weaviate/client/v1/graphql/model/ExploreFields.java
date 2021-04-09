package technology.semi.weaviate.client.v1.graphql.model;

public enum ExploreFields {
  CERTAINTY("certainty"),
  BEACON("beacon"),
  CLASS_NAME("className");

  private final String field;

  ExploreFields(String field) {
    this.field = field;
  }

  @Override
  public String toString() {
    return field;
  }
}
