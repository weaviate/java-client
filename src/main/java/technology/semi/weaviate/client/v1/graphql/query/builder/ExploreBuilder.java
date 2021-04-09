package technology.semi.weaviate.client.v1.graphql.query.builder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.model.ExploreFields;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExploreBuilder implements Query {
  ExploreFields[] fields;
  NearTextArgument withNearText;

  private String createFilterClause() {
    HashSet<String> filters = new LinkedHashSet<>();
    if (withNearText != null) {
      filters.add(withNearText.build());
    }
    return String.format("%s", StringUtils.joinWith(", ", filters.toArray()));
  }

  @Override
  public String buildQuery() {
    String fieldsClause = "";
    if (fields != null && fields.length > 0) {
      fieldsClause = StringUtils.joinWith(", ", fields);
    }
    String filterClause = createFilterClause();
    return String.format("{Explore(%s){%s}}", filterClause, fieldsClause);
  }
}
