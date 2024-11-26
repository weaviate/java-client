package io.weaviate.client.v1.async.graphql.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseGraphQLClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLGetBaseObject;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;
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
import io.weaviate.client.v1.graphql.query.argument.SortArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArguments;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.graphql.query.fields.GenerativeSearchBuilder;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class Get extends AsyncBaseGraphQLClient<GraphQLResponse> implements AsyncClientResult<GraphQLResponse> {
  private final GetBuilder.GetBuilderBuilder getBuilder;

  public Get(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
    getBuilder = GetBuilder.builder();
  }

  public Get withClassName(String className) {
    getBuilder.className(className);
    return this;
  }

  public Get withFields(Field... fields) {
    getBuilder.fields(Fields.builder()
      .fields(fields)
      .build());
    return this;
  }

  @Deprecated
  public Get withWhere(WhereFilter where) {
    return withWhere(WhereArgument.builder()
      .filter(where)
      .build());
  }

  public Get withWhere(WhereArgument where) {
    getBuilder.withWhereFilter(where);
    return this;
  }

  public Get withLimit(Integer limit) {
    getBuilder.limit(limit);
    return this;
  }

  public Get withOffset(Integer offset) {
    getBuilder.offset(offset);
    return this;
  }

  public Get withAfter(String after) {
    getBuilder.after(after);
    return this;
  }

  public Get withBm25(Bm25Argument bm25) {
    getBuilder.withBm25Filter(bm25);
    return this;
  }

  public Get withHybrid(HybridArgument hybrid) {
    getBuilder.withHybridFilter(hybrid);
    return this;
  }

  public Get withAsk(AskArgument ask) {
    getBuilder.withAskArgument(ask);
    return this;
  }

  public Get withNearText(NearTextArgument nearText) {
    getBuilder.withNearTextFilter(nearText);
    return this;
  }

  public Get withNearObject(NearObjectArgument nearObject) {
    getBuilder.withNearObjectFilter(nearObject);
    return this;
  }

  public Get withNearVector(NearVectorArgument nearVector) {
    getBuilder.withNearVectorFilter(nearVector);
    return this;
  }

  public Get withNearImage(NearImageArgument nearImage) {
    getBuilder.withNearImageFilter(nearImage);
    return this;
  }

  public Get withNearAudio(NearAudioArgument nearAudio) {
    getBuilder.withNearAudioFilter(nearAudio);
    return this;
  }

  public Get withNearVideo(NearVideoArgument nearVideo) {
    getBuilder.withNearVideoFilter(nearVideo);
    return this;
  }

  public Get withNearDepth(NearDepthArgument nearDepth) {
    getBuilder.withNearDepthFilter(nearDepth);
    return this;
  }

  public Get withNearThermal(NearThermalArgument nearThermal) {
    getBuilder.withNearThermalFilter(nearThermal);
    return this;
  }

  public Get withNearImu(NearImuArgument nearImu) {
    getBuilder.withNearImuFilter(nearImu);
    return this;
  }

  public Get withGroup(GroupArgument group) {
    getBuilder.withGroupArgument(group);
    return this;
  }

  public Get withSort(SortArgument... sort) {
    getBuilder.withSortArguments(SortArguments.builder()
      .sort(sort)
      .build());
    return this;
  }

  public Get withGenerativeSearch(GenerativeSearchBuilder generativeSearch) {
    getBuilder.withGenerativeSearch(generativeSearch);
    return this;
  }

  public Get withConsistencyLevel(String level) {
    getBuilder.withConsistencyLevel(level);
    return this;
  }

  public Get withGroupBy(GroupByArgument groupBy) {
    getBuilder.withGroupByArgument(groupBy);
    return this;
  }

  public Get withTenant(String tenant) {
    getBuilder.tenant(tenant);
    return this;
  }

  public Get withAutocut(Integer autocut) {
    getBuilder.autocut(autocut);
    return this;
  }

  private GraphQLQuery getQuery() {
    String getQuery = getBuilder.build()
      .buildQuery();
    return GraphQLQuery.builder()
      .query(getQuery)
      .build();
  }

  @Override
  public Future<Result<GraphQLResponse>> run(FutureCallback<Result<GraphQLResponse>> callback) {
    return sendPostRequest("/graphql", getQuery(), GraphQLResponse.class, callback);
  }

  /**
   * This method provides a better way of serializing a GraphQL response using one's defined classes.
   * Example:
   * In Weaviate we have defined collection named Soup with name and price properties.
   * For client to be able to properly serialize GraphQL response to an Object with
   * convenient methods accessing GraphQL settings one can create a class, example:
   * <pre>{@code
   * import com.google.gson.annotations.SerializedName;
   *
   * public class Soups {
   *   {@literal @}SerializedName(value = "Soup")
   *   List<Soup> soups;
   *
   *   public List<Soup> getSoups() {
   *     return soups;
   *   }
   *
   *   public static class Soup extends GraphQLGetBaseObject {
   *     String name;
   *     Float price;
   *
   *     public String getName() {
   *       return name;
   *     }
   *
   *     public Float getPrice() {
   *       return price;
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param classOfC - class describing Weaviate object, example: Soups class
   * @param <C>      - Class of C
   * @return Result of GraphQLTypedResponse of a given class
   * @see GraphQLGetBaseObject
   */
  public <C> Future<Result<GraphQLTypedResponse<C>>> run(final Class<C> classOfC) {
    return run(classOfC, null);
  }

  /**
   * This method provides a better way of serializing a GraphQL response using one's defined classes.
   * Example:
   * In Weaviate we have defined collection named Soup with name and price properties.
   * For client to be able to properly serialize GraphQL response to an Object with
   * convenient methods accessing GraphQL settings one can create a class, example:
   * <pre>{@code
   * import com.google.gson.annotations.SerializedName;
   *
   * public class Soups {
   *   {@literal @}SerializedName(value = "Soup")
   *   List<Soup> soups;
   *
   *   public List<Soup> getSoups() {
   *     return soups;
   *   }
   *
   *   public static class Soup extends GraphQLGetBaseObject {
   *     String name;
   *     Float price;
   *
   *     public String getName() {
   *       return name;
   *     }
   *
   *     public Float getPrice() {
   *       return price;
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param classOfC - class describing Weaviate object, example: Soups class
   * @param callback - Result of GraphQLTypedResponse of a given class callback
   * @param <C>      - Class of C
   * @return Result of GraphQLTypedResponse of a given class
   * @see GraphQLGetBaseObject
   */
  public <C> Future<Result<GraphQLTypedResponse<C>>> run(final Class<C> classOfC, FutureCallback<Result<GraphQLTypedResponse<C>>> callback) {
    return sendGraphQLTypedRequest(getQuery(), classOfC, callback);
  }
}
