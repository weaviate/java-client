package technology.semi.weaviate.client.v1.graphql.query.builder;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArguments;
import technology.semi.weaviate.client.v1.graphql.query.argument.WhereArgument;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GetBuilder implements Query {
  String className;
  Fields fields;
  Integer offset;
  Integer limit;
  WhereArgument withWhereArgument;
  NearTextArgument withNearTextFilter;
  NearObjectArgument withNearObjectFilter;
  AskArgument withAskArgument;
  NearImageArgument withNearImageFilter;
  NearVectorArgument withNearVectorFilter;
  GroupArgument withGroupArgument;
  SortArguments withSortArguments;

  private boolean includesFilterClause() {
    return ObjectUtils.anyNotNull(withWhereArgument, withNearTextFilter, withNearObjectFilter,
            withNearVectorFilter, withNearImageFilter, withGroupArgument, withAskArgument,
            limit, withSortArguments);
  }

  private String createFilterClause() {
    if (includesFilterClause()) {
      Set<String> filters = new LinkedHashSet<>();
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
      if (withGroupArgument != null) {
        filters.add(withGroupArgument.build());
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
      if (offset != null) {
        filters.add(String.format("offset: %s", offset));
      }
      if (withSortArguments != null) {
        filters.add(withSortArguments.build());
      }
      return String.format("(%s)", StringUtils.joinWith(", ", filters.toArray()));
    }
    return "";
  }

  private String createFields() {
    return fields != null ? fields.build() : "";
  }

  @Override
  public String buildQuery() {
    return String.format("{Get{%s%s{%s}}}", className, createFilterClause(), createFields());
  }
}
