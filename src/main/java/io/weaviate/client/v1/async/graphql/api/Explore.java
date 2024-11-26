package io.weaviate.client.v1.async.graphql.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.graphql.model.ExploreFields;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.*;
import io.weaviate.client.v1.graphql.query.builder.ExploreBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.Future;

public class Explore extends AsyncBaseClient<GraphQLResponse> implements AsyncClientResult<GraphQLResponse> {
  private final ExploreBuilder.ExploreBuilderBuilder exploreBuilder;

  public Explore(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
    exploreBuilder = ExploreBuilder.builder();
  }

  public Explore withFields(ExploreFields... fields) {
    exploreBuilder.fields(fields);
    return this;
  }

  public Explore withLimit(Integer limit) {
    exploreBuilder.limit(limit);
    return this;
  }

  public Explore withOffset(Integer offset) {
    exploreBuilder.offset(offset);
    return this;
  }

  public Explore withAsk(AskArgument ask) {
    exploreBuilder.withAskArgument(ask);
    return this;
  }

  public Explore withNearText(NearTextArgument nearText) {
    exploreBuilder.withNearText(nearText);
    return this;
  }

  public Explore withNearObject(NearObjectArgument nearObject) {
    exploreBuilder.withNearObjectFilter(nearObject);
    return this;
  }

  public Explore withNearVector(NearVectorArgument nearVector) {
    exploreBuilder.withNearVectorFilter(nearVector);
    return this;
  }

  public Explore withNearImage(NearImageArgument nearImage) {
    exploreBuilder.withNearImageFilter(nearImage);
    return this;
  }

  public Explore withNearAudio(NearAudioArgument nearAudio) {
    exploreBuilder.withNearAudioFilter(nearAudio);
    return this;
  }

  public Explore withNearVideo(NearVideoArgument nearVideo) {
    exploreBuilder.withNearVideoFilter(nearVideo);
    return this;
  }

  public Explore withNearDepth(NearDepthArgument nearDepth) {
    exploreBuilder.withNearDepthFilter(nearDepth);
    return this;
  }

  public Explore withNearThermal(NearThermalArgument nearThermal) {
    exploreBuilder.withNearThermalFilter(nearThermal);
    return this;
  }

  public Explore withNearImu(NearImuArgument nearImu) {
    exploreBuilder.withNearImuFilter(nearImu);
    return this;
  }

  @Override
  public Future<Result<GraphQLResponse>> run(FutureCallback<Result<GraphQLResponse>> callback) {
    String exploreQuery = exploreBuilder.build()
      .buildQuery();
    GraphQLQuery query = GraphQLQuery.builder()
      .query(exploreQuery)
      .build();
    return sendPostRequest("/graphql", query, GraphQLResponse.class, callback);
  }
}
