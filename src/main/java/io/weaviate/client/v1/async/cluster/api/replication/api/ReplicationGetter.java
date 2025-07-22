package io.weaviate.client.v1.async.cluster.api.replication.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;

public class ReplicationGetter extends AsyncBaseClient<ReplicateOperation>
    implements AsyncClientResult<ReplicateOperation> {
  private String uuid;
  private Boolean includeHistory;

  public ReplicationGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public ReplicationGetter withUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  public ReplicationGetter withIncludeHistory(boolean includeHistory) {
    this.includeHistory = includeHistory;
    return this;
  }

  @Override
  public Future<Result<ReplicateOperation>> run(FutureCallback<Result<ReplicateOperation>> callback) {
    String path = "/replication/replicate/" + uuid;
    if (includeHistory != null) {
      path += "?" + UrlEncoder.encodeQueryParam("includeHistory", includeHistory.toString());
    }
    return sendGetRequest(path, ReplicateOperation.class, callback);
  }
}
