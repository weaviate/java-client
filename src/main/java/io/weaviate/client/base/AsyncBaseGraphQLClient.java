package io.weaviate.client.base;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.async.WeaviateGraphQLTypedResponseConsumer;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class AsyncBaseGraphQLClient<T> extends AsyncBaseClient<T> {
  public AsyncBaseGraphQLClient(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  protected <C> Future<Result<GraphQLTypedResponse<C>>> sendGraphQLTypedRequest(Object payload, Class<C> classOfC,
    FutureCallback<Result<GraphQLTypedResponse<C>>> callback) {
    return client.execute(SimpleRequestProducer.create(getRequest("/graphql", payload, "POST")), new WeaviateGraphQLTypedResponseConsumer<>(classOfC), callback);
  }
}
