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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.BooleanArray;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.Filters;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.IntArray;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.NumberArray;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.TextArray;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.NearVector;
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
import io.weaviate.client.v1.grpc.GRPC;
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

    if (this.withWhereFilter != null) {
      Filters.Builder filters = Filters.newBuilder();
      addWhereFilters(filters, this.withWhereFilter.getFilter());
      search.setFilters(filters.build());
    }

    if (this.withNearVectorFilter != null) {
      NearVector.Builder nearVector = NearVector.newBuilder();
      NearVectorArgument f = this.withNearVectorFilter;

      Float[] vector = f.getVector();
      if (vector != null) {
        nearVector.setVectorBytes(GRPC.toByteString(f.getVector()));
      }

      if (f.getCertainty() != null) {
        nearVector.setCertainty(f.getCertainty());
      } else if (f.getDistance() != null) {
        nearVector.setDistance(f.getDistance());
      }

      search.setNearVector(nearVector.build());
    }

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
      List<Field> props = Arrays.stream(fields.getFields())
          .filter(f -> !"_additional".equals(f.getName())).toList();
      if (!props.isEmpty()) {
        PropertiesRequest.Builder properties = PropertiesRequest.newBuilder();
        for (Field f : props) {
          properties.addNonRefProperties(f.getName());
        }
        search.setProperties(properties.build());
      }
    }

    search.setUses123Api(true);
    search.setUses125Api(true);
    search.setUses127Api(true);
    return search.build();
  }

  private void addWhereFilters(Filters.Builder where, WhereFilter f) {
    WhereFilter[] operands = f.getOperands();

    if (ArrayUtils.isNotEmpty(operands)) { // Nested filters
      for (WhereFilter op : operands) {
        addWhereFilters(where, op);
      }
    } else { // Individual where clauses (leaves)
      if (ArrayUtils.isNotEmpty(f.getPath())) {
        // Deprecated, but the current proto doesn't have 'path'.
        where.addOn(f.getPath()[0]);
      }
      if (f.getValueBoolean() != null) {
      } else if (f.getValueBooleanArray() != null) {
        BooleanArray.Builder arr = BooleanArray.newBuilder();
        Arrays.stream(f.getValueBooleanArray()).forEach(v -> arr.addValues(v));
        where.setValueBooleanArray(arr.build());
      } else if (f.getValueInt() != null) {
        where.setValueInt(f.getValueInt());
      } else if (f.getValueIntArray() != null) {
        IntArray.Builder arr = IntArray.newBuilder();
        Arrays.stream(f.getValueIntArray()).forEach(v -> arr.addValues(v));
        where.setValueIntArray(arr.build());
      } else if (f.getValueNumber() != null) {
        where.setValueNumber(f.getValueNumber());
      } else if (f.getValueNumberArray() != null) {
        NumberArray.Builder arr = NumberArray.newBuilder();
        Arrays.stream(f.getValueNumberArray()).forEach(v -> arr.addValues(v));
        where.setValueNumberArray(arr.build());
      } else if (f.getValueText() != null) {
        where.setValueText(f.getValueText());
      } else if (f.getValueTextArray() != null) {
        TextArray.Builder arr = TextArray.newBuilder();
        Arrays.stream(f.getValueTextArray()).forEach(v -> arr.addValues(v));
        where.setValueTextArray(arr.build());
      }
    }

    switch (f.getOperator()) {
      case Operator.And:
        where.setOperator(WeaviateProtoBase.Filters.Operator.OPERATOR_AND);
        break;
      case Operator.Or:
        where.setOperator(WeaviateProtoBase.Filters.Operator.OPERATOR_OR);
        break;
    }
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
