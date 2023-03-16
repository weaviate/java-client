package io.weaviate.client.v1.graphql.query.builder;

import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.v1.graphql.model.ExploreFields;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExploreBuilder implements Query {
  ExploreFields[] fields;
  Integer offset;
  Integer limit;
  NearTextArgument withNearText;
  NearObjectArgument withNearObjectFilter;
  AskArgument withAskArgument;
  NearImageArgument withNearImageFilter;
  NearVectorArgument withNearVectorFilter;

  private String createFilterClause() {
    Set<String> filters = new LinkedHashSet<>();

    if (withNearText != null) {
      filters.add(withNearText.build());
    }
    if (withNearObjectFilter != null) {
      filters.add(withNearObjectFilter.build());
    }
    if (withNearVectorFilter != null) {
      filters.add(withNearVectorFilter.build());
    }
    if (withAskArgument != null) {
      filters.add(withAskArgument.build());
    }
    if (withNearImageFilter != null) {
      filters.add(withNearImageFilter.build());
    }
    if (limit != null) {
      filters.add(String.format("limit:%s", limit));
    }
    if (offset != null) {
      filters.add(String.format("offset:%s", offset));
    }

    return String.format("%s", String.join(" ", filters));
  }

  @Override
  public String buildQuery() {
    String fieldsClause = "";
    if (ArrayUtils.isNotEmpty(fields)) {
      fieldsClause = StringUtils.joinWith(",", (Object[]) fields);
    }
    String filterClause = createFilterClause();
    return String.format("{Explore(%s){%s}}", filterClause, fieldsClause);
  }
}
