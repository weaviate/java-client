package io.weaviate.client.v1.async.misc.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.misc.model.OpenIDConfiguration;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class OpenIDConfigGetter extends AsyncBaseClient<OpenIDConfiguration> implements AsyncClientResult<OpenIDConfiguration> {

  public OpenIDConfigGetter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  @Override
  public Future<Result<OpenIDConfiguration>> run(FutureCallback<Result<OpenIDConfiguration>> callback) {
    return sendGetRequest("/meta", OpenIDConfiguration.class, callback);
  }
}
