package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.schema.model.ShardStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

public class ShardsUpdater extends AsyncBaseClient<ShardStatus> implements AsyncClientResult<ShardStatus> {
  private String className;
  private String shardName;
  private ShardStatus status;

  public ShardsUpdater(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }

  public ShardsUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ShardsUpdater withShardName(String shardName) {
    this.shardName = shardName;
    return this;
  }

  public ShardsUpdater withStatus(String targetStatus) {
    this.status = ShardStatus.builder().status(targetStatus).build();
    return this;
  }

  @Override
  public Future<Result<ShardStatus>> run(FutureCallback<Result<ShardStatus>> callback) {
    List<String> emptyFieldNames = new ArrayList<>();
    if (StringUtils.isEmpty(this.className)) {
      emptyFieldNames.add("className");
    }
    if (StringUtils.isEmpty(this.shardName)) {
      emptyFieldNames.add("shardName");
    }
    if (this.status == null) {
      emptyFieldNames.add("status");
    }
    if (emptyFieldNames.size() > 0) {
      String message = String.format("%s cannot be empty", StringUtils.joinWith(", ", emptyFieldNames.toArray()));
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message(message).build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Collections.singletonList(errorMessage)).build();
      return CompletableFuture.completedFuture(new Result<>(HttpStatus.SC_BAD_REQUEST, null, errors));
    }
    String path = String.format("/schema/%s/shards/%s", this.className, this.shardName);
    return sendPostRequest(path, status, ShardStatus.class, callback);
  }
}
