package technology.semi.weaviate.client.v1.graphql.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GraphQLErrorLocationsItems {
  Long column;
  Long line;
}
