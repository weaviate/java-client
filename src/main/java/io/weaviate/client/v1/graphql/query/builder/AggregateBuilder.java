package io.weaviate.client.v1.graphql.query.builder;

import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.query.argument.Argument;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearAudioArgument;
import io.weaviate.client.v1.graphql.query.argument.NearDepthArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImuArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearThermalArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateBuilder implements Query {
  String className;
  Fields fields;
  String groupByClausePropertyName;
  WhereArgument withWhereFilter;
  AskArgument withAskArgument;
  NearTextArgument withNearTextFilter;
  NearObjectArgument withNearObjectFilter;
  NearVectorArgument withNearVectorFilter;
  NearImageArgument withNearImageFilter;
  NearAudioArgument withNearAudioFilter;
  NearVideoArgument withNearVideoFilter;
  NearDepthArgument withNearDepthFilter;
  NearThermalArgument withNearThermalFilter;
  NearImuArgument withNearImuFilter;
  Integer objectLimit;
  Integer limit;
  String tenant;

  private Stream<Argument> buildableArguments() {
    return Stream.of(withWhereFilter, withAskArgument, withNearTextFilter, withNearObjectFilter,
      withNearVectorFilter, withNearImageFilter, withNearAudioFilter, withNearVideoFilter, withNearDepthFilter,
      withNearThermalFilter, withNearImuFilter);
  }

  private Stream<Object> nonStringArguments() {
    return Stream.of(objectLimit, limit);
  }

  private Stream<String> stringArguments() {
    return Stream.of(groupByClausePropertyName, tenant);
  }

  private boolean includesFilterClause() {
    return buildableArguments().anyMatch(Objects::nonNull)
      || nonStringArguments().anyMatch(Objects::nonNull)
      || stringArguments().anyMatch(StringUtils::isNotBlank);
  }

  private String createFilterClause() {
    if (includesFilterClause()) {
      Set<String> filters = new LinkedHashSet<>();

      if (StringUtils.isNotBlank(tenant)) {
        filters.add(String.format("tenant:%s", Serializer.quote(tenant)));
      }
      if (StringUtils.isNotBlank(groupByClausePropertyName)) {
        filters.add(String.format("groupBy:%s", Serializer.quote(groupByClausePropertyName)));
      }

      buildableArguments()
        .filter(Objects::nonNull)
        .map(Argument::build)
        .forEach(filters::add);

      if (limit != null) {
        filters.add(String.format("limit:%s", limit));
      }
      if (objectLimit != null) {
        filters.add(String.format("objectLimit:%s", objectLimit));
      }

      return String.format("(%s)", String.join(" ", filters));
    }
    return "";
  }

  @Override
  public String buildQuery() {
    String fieldsClause = fields != null ? fields.build() : "";
    return String.format("{Aggregate{%s%s{%s}}}", Serializer.escape(className), createFilterClause(), fieldsClause);
  }


  // created to support both types of setters: WhereArgument and deprecated WhereFilter
  public static class AggregateBuilderBuilder {
    private WhereArgument withWhereFilter;

    @Deprecated
    public AggregateBuilderBuilder withWhereFilter(WhereFilter whereFilter) {
      this.withWhereFilter = WhereArgument.builder().filter(whereFilter).build();
      return this;
    }

    public AggregateBuilderBuilder withWhereFilter(WhereArgument whereArgument) {
      this.withWhereFilter = whereArgument;
      return this;
    }
  }
}
