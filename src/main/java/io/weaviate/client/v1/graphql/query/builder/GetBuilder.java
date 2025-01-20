package io.weaviate.client.v1.graphql.query.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.Filters;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.query.argument.Argument;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import io.weaviate.client.v1.graphql.query.argument.GroupArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupByArgument;
import io.weaviate.client.v1.graphql.query.argument.HybridArgument;
import io.weaviate.client.v1.graphql.query.argument.NearAudioArgument;
import io.weaviate.client.v1.graphql.query.argument.NearDepthArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImuArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearThermalArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArguments;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.graphql.query.fields.GenerativeSearchBuilder;
import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GetBuilder implements Query {
  String className;
  Fields fields;
  Integer offset;
  Integer limit;
  String after;
  Integer autocut;
  String withConsistencyLevel;
  WhereArgument withWhereFilter;
  Bm25Argument withBm25Filter;
  HybridArgument withHybridFilter;
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
  GroupArgument withGroupArgument;
  SortArguments withSortArguments;
  GenerativeSearchBuilder withGenerativeSearch;
  GroupByArgument withGroupByArgument;
  String tenant;

  private Stream<Argument> buildableArguments() {
    return Stream.of(withWhereFilter, withAskArgument, withNearTextFilter, withNearObjectFilter,
        withNearVectorFilter, withGroupArgument, withBm25Filter, withHybridFilter, withSortArguments,
        withGroupByArgument,
        withNearImageFilter, withNearAudioFilter, withNearVideoFilter, withNearDepthFilter, withNearThermalFilter,
        withNearImuFilter);
  }

  private Stream<Argument> buildableGrpcArguments() {
    return Stream.of(withWhereFilter, withNearVectorFilter);
  }

  private Stream<Object> nonStringArguments() {
    return Stream.of(limit, offset, autocut);
  }

  private Stream<String> stringArguments() {
    return Stream.of(withConsistencyLevel, after, tenant);
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

      buildableArguments()
          .filter(Objects::nonNull)
          .map(Argument::build)
          .forEach(filters::add);

      if (limit != null) {
        filters.add(String.format("limit:%s", limit));
      }
      if (offset != null) {
        filters.add(String.format("offset:%s", offset));
      }
      if (StringUtils.isNotBlank(after)) {
        filters.add(String.format("after:%s", Serializer.quote(after)));
      }
      if (StringUtils.isNotBlank(withConsistencyLevel)) {
        filters.add(String.format("consistencyLevel:%s", Serializer.escape(withConsistencyLevel)));
      }
      if (autocut != null) {
        filters.add(String.format("autocut:%s", autocut));
      }

      return String.format("(%s)", String.join(" ", filters));
    }
    return "";
  }

  private String createFields() {
    if (ObjectUtils.allNull(fields, withGenerativeSearch)) {
      return "";
    }

    if (withGenerativeSearch == null) {
      return fields.build();
    }

    Field generate = withGenerativeSearch.build();
    Field generateAdditional = Field.builder()
        .name("_additional")
        .fields(new Field[] { generate })
        .build();

    if (fields == null) {
      return generateAdditional.build();
    }

    // check if _additional field exists. If missing just add new _additional with
    // generate,
    // if exists merge generate into present one
    Map<Boolean, List<Field>> grouped = Arrays.stream(fields.getFields())
        .collect(Collectors.groupingBy(f -> "_additional".equals(f.getName())));

    List<Field> additionals = grouped.getOrDefault(true, new ArrayList<>());
    if (additionals.isEmpty()) {
      additionals.add(generateAdditional);
    } else {
      Field[] mergedInternalFields = Stream.concat(
          Arrays.stream(additionals.get(0).getFields()),
          Stream.of(generate)).toArray(Field[]::new);

      additionals.set(0, Field.builder()
          .name("_additional")
          .fields(mergedInternalFields)
          .build());
    }

    Field[] allFields = Stream.concat(
        grouped.getOrDefault(false, new ArrayList<>()).stream(),
        additionals.stream()).toArray(Field[]::new);

    return Fields.builder()
        .fields(allFields)
        .build()
        .build();
  }

  @Override
  public String buildQuery() {
    return String.format("{Get{%s%s{%s}}}", Serializer.escape(className), createFilterClause(), createFields());
  }

  public SearchRequest buildSearchRequest() {
    SearchRequest.Builder search = SearchRequest.newBuilder();

    search.setCollection(this.className);

    if (StringUtils.isNotBlank(tenant)) {
      search.setTenant(this.tenant);
    }

    // TODO: Create filter clause
    if (includesFilterClause()) {

      Filters.Builder filters = Filters.newBuilder();
      if (this.withWhereFilter != null) {
        WhereFilter f = this.withWhereFilter.getFilter();
        switch (f.getOperator()) {
          case Operator.And:
            filters.setOperator(WeaviateProtoBase.Filters.Operator.OPERATOR_AND);
            break;
        }

        WhereFilter[] operands = f.getOperands();
        if (operands != null && operands.length > 0) {
        }
      }

      search.setFilters(filters.build());

      // withWhereFilter, withNearVectorFilter
      //
      buildableGrpcArguments()
          .filter(Objects::nonNull)
          .forEach(arg -> arg.addToSearch(search));

      if (limit != null) {
        search.setLimit(limit);
      }
      if (offset != null) {
        search.setOffset(offset);
      }
      if (StringUtils.isNotBlank(after)) {
        search.setAfter(after);
      }
      if (StringUtils.isNotBlank(withConsistencyLevel)) {
        search.setConsistencyLevelValue(Integer.valueOf(withConsistencyLevel));
      }
      if (autocut != null) {
        search.setAutocut(autocut);
      }
    }

    // Create fields
    if (fields != null) {

      // Metadata
      Optional<Field> _additional = Arrays.stream(fields.getFields())
          .filter(f -> "_additional".equals(f.getName())).findFirst();
      if (_additional.isPresent()) {
        MetadataRequest.Builder metadata = MetadataRequest.newBuilder();
        for (Field f : _additional.get().getFields()) {
          switch (f.getName()) {
            case "id":
              metadata.setUuid(true);
              break;
            case "vector":
              metadata.setVector(true);
              break;
            case "distance":
              metadata.setDistance(true);
              break;
          }
        }
        search.setMetadata(metadata.build());
      }

      // Properties
      Optional<Field> props = Arrays.stream(fields.getFields())
          .filter(f -> !"_additional".equals(f.getName())).findFirst();
      if (props.isPresent()) {
        PropertiesRequest.Builder properties = PropertiesRequest.newBuilder();
        int i = 0;
        for (Field f : props.get().getFields()) {
          properties.setNonRefProperties(i++, f.getName());
        }
        search.setProperties(properties.build());
      }
    }
    return search.build();
  }

  // created to support both types of setters: WhereArgument and deprecated
  // WhereFilter
  public static class GetBuilderBuilder {
    private WhereArgument withWhereFilter;

    @Deprecated
    public GetBuilderBuilder withWhereFilter(WhereFilter whereFilter) {
      this.withWhereFilter = WhereArgument.builder().filter(whereFilter).build();
      return this;
    }

    public GetBuilderBuilder withWhereFilter(WhereArgument whereArgument) {
      this.withWhereFilter = whereArgument;
      return this;
    }
  }
}
