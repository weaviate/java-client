package technology.semi.weaviate.client.v1.graphql.query.builder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GetBuilder implements Query {
  String className;
  String fields;
  String withWhereFilter;
  Integer limit;
  NearTextArgument withNearTextFilter;
  NearObjectArgument withNearObjectFilter;
  AskArgument withAskArgument;
  Float[] withNearVectorFilter;
  String withGroupFilter;

  private boolean includesFilterClause() {
    return StringUtils.isNotBlank(withWhereFilter)
            || withNearTextFilter != null || withNearObjectFilter != null
            || (withNearVectorFilter != null && withNearVectorFilter.length > 0)
            || StringUtils.isNotBlank(withGroupFilter)
            || withAskArgument != null
            || limit != null;
  }

  private String createFilterClause() {
    HashSet<String> filters = new LinkedHashSet<>();
    if (StringUtils.isNotBlank(withWhereFilter)) {
      filters.add(String.format("where: %s", withWhereFilter));
    }
    if (withNearTextFilter != null) {
      filters.add(withNearTextFilter.build());
    }
    if (withNearObjectFilter != null) {
      filters.add(withNearObjectFilter.build());
    }
    if (withNearVectorFilter != null && withNearVectorFilter.length > 0) {
      filters.add(String.format("nearVector: {vector: [%s]}", StringUtils.joinWith(",", withNearVectorFilter)));
    }
    if (StringUtils.isNotBlank(withGroupFilter)) {
      filters.add(String.format("group: %s", withGroupFilter));
    }
    if (withAskArgument != null) {
      filters.add(withAskArgument.build());
    }
    if (limit != null) {
      filters.add(String.format("limit: %s", limit));
    }
    return String.format("(%s)", StringUtils.joinWith(", ", filters.toArray()));
  }

  @Override
  public String buildQuery() {
    String filterClause = "";
    if (includesFilterClause()) {
      filterClause = createFilterClause();
    }
    return String.format("{Get {%s %s {%s}}}", className, filterClause, fields);
  }
}
