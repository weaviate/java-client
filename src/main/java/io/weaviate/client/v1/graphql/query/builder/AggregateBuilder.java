package io.weaviate.client.v1.graphql.query.builder;

import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.filters.WhereFilterUtil;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateBuilder implements Query {
  String className;
  Fields fields;
  String groupByClausePropertyName;
  WhereFilter withWhereFilter;
  NearTextArgument withNearTextFilter;
  NearObjectArgument withNearObjectFilter;
  NearVectorArgument withNearVectorFilter;
  AskArgument withAskArgument;
  NearImageArgument withNearImageFilter;
  Integer objectLimit;
  Integer limit;

  private boolean includesFilterClause() {
    return ObjectUtils.anyNotNull(withWhereFilter, withNearTextFilter, withNearObjectFilter,
      withNearVectorFilter, objectLimit, withAskArgument, withNearImageFilter, limit)
      || StringUtils.isNotBlank(groupByClausePropertyName);
  }

  private String createFilterClause() {
    if (includesFilterClause()) {
      Set<String> filters = new LinkedHashSet<>();
      if (StringUtils.isNotBlank(groupByClausePropertyName)) {
        filters.add(String.format("groupBy: \"%s\"", groupByClausePropertyName));
      }
      if (withWhereFilter != null) {
        filters.add(WhereFilterUtil.toGraphQLString(withWhereFilter));
      }
      if (withNearTextFilter != null) {
        filters.add(withNearTextFilter.build());
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
        filters.add(String.format("limit: %s", limit));
      }
      if (objectLimit != null) {
        filters.add(String.format("objectLimit: %s", objectLimit));
      }
      return String.format("(%s)", StringUtils.joinWith(", ", filters.toArray()));
    }
    return "";
  }

  @Override
  public String buildQuery() {
    String fieldsClause = fields != null ? fields.build() : "";
    return String.format("{Aggregate{%s%s{%s}}}", className, createFilterClause(), fieldsClause);
  }
}
