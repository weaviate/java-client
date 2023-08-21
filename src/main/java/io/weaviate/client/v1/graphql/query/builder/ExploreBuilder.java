package io.weaviate.client.v1.graphql.query.builder;

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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.v1.graphql.model.ExploreFields;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExploreBuilder implements Query {
  ExploreFields[] fields;
  Integer offset;
  Integer limit;
  AskArgument withAskArgument;
  NearTextArgument withNearText;
  NearObjectArgument withNearObjectFilter;
  NearVectorArgument withNearVectorFilter;
  NearImageArgument withNearImageFilter;
  NearAudioArgument withNearAudioFilter;
  NearVideoArgument withNearVideoFilter;
  NearDepthArgument withNearDepthFilter;
  NearThermalArgument withNearThermalFilter;
  NearImuArgument withNearImuFilter;

  private String createFilterClause() {
    Set<String> filters = new LinkedHashSet<>();

    Stream.of(withAskArgument, withNearText, withNearObjectFilter, withNearVectorFilter, withNearImageFilter,
        withNearAudioFilter, withNearVideoFilter, withNearDepthFilter, withNearThermalFilter, withNearImuFilter)
      .filter(Objects::nonNull)
      .map(Argument::build)
      .forEach(filters::add);

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
