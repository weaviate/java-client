package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.schema.model.Schema;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class SchemaGetter extends AsyncBaseClient<Schema> implements AsyncClientResult<Schema> {

  public SchemaGetter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    super(client, config, tokenProvider);
  }

  @Override
  public Future<Result<Schema>> run(FutureCallback<Result<Schema>> callback) {
    return sendGetRequest("/schema", Schema.class, callback);
  }
}
