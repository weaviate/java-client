package technology.semi.weaviate.client.v1.graphql.query.builder;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RawGQLBuilder implements Query {
  String query;

  @Override
  public String buildQuery() {
    return query;
  }
}
