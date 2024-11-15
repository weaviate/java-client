package io.weaviate.client.v1.async.classifications.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.classifications.model.Classification;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Getter extends AsyncBaseClient<Classification>
  implements AsyncClientResult<Classification> {

  private String id;


  public Getter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }


  public Getter withID(String id) {
    this.id = id;
    return this;
  }


  @Override
  public Future<Result<Classification>> run(FutureCallback<Result<Classification>> callback) {
    if (StringUtils.isBlank(id)) {
      return CompletableFuture.completedFuture(null);
    }
    String path = String.format("/classifications/%s", UrlEncoder.encodePathParam(id));
    return sendGetRequest(path, Classification.class, callback);
  }
}
