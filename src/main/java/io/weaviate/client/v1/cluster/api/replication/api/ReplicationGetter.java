package io.weaviate.client.v1.cluster.api.replication.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;

public class ReplicationGetter extends BaseClient<ReplicateOperation> implements ClientResult<ReplicateOperation> {
  private String uuid;
  private Boolean includeHistory;

  public ReplicationGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
  public Result<ReplicateOperation> run() {
    String path = "/replication/replicate/" + uuid;
    if (includeHistory != null) {
      path += "?" + UrlEncoder.encodeQueryParam("includeHistory", includeHistory.toString());
    }
    return new Result<>(sendGetRequest(path, ReplicateOperation.class));
  }
}
