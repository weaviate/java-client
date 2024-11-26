package io.weaviate.client.v1.async.misc.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.misc.model.Meta;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class MetaGetter extends AsyncBaseClient<Meta> implements AsyncClientResult<Meta> {

  public MetaGetter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  @Override
  public Future<Result<Meta>> run(FutureCallback<Result<Meta>> callback) {
    return sendGetRequest("/meta", Meta.class, callback);
  }
}
