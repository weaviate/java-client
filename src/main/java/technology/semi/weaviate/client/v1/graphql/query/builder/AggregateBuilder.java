package technology.semi.weaviate.client.v1.graphql.query.builder;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.WhereArgument;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateBuilder implements Query {
  String className;
  Fields fields;
  String groupByClausePropertyName;
  WhereArgument withWhereArgument;
  NearTextArgument withNearTextFilter;
  NearObjectArgument withNearObjectFilter;
  NearVectorArgument withNearVectorFilter;
  Integer objectLimit;

  private boolean includesFilterClause() {
    return ObjectUtils.anyNotNull(withWhereArgument, withNearTextFilter, withNearObjectFilter,
            withNearVectorFilter, objectLimit) || StringUtils.isNotBlank(groupByClausePropertyName);
  }

  private String createFilterClause() {
    if (includesFilterClause()) {
      Set<String> filters = new LinkedHashSet<>();
      if (StringUtils.isNotBlank(groupByClausePropertyName)) {
        filters.add(String.format("groupBy: \"%s\"", groupByClausePropertyName));
      }
      if (withWhereArgument != null) {
        filters.add(withWhereArgument.build());
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
