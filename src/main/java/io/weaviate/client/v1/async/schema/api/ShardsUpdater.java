package io.weaviate.client.v1.async.schema.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.schema.model.Shard;
import io.weaviate.client.v1.schema.model.ShardStatus;

public class ShardsUpdater extends AsyncBaseClient<ShardStatus> implements AsyncClientResult<ShardStatus[]> {
  private final ShardsGetter shardsGetter;
  private final ShardUpdater shardUpdater;

  private String className;
  private String status;

  public ShardsUpdater(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
    this.shardsGetter = new ShardsGetter(client, config);
    this.shardUpdater = new ShardUpdater(client, config);
  }

  public ShardsUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ShardsUpdater withStatus(String targetStatus) {
    this.status = targetStatus;
    return this;
  }

  @Override
  public Future<Result<ShardStatus[]>> run(FutureCallback<Result<ShardStatus[]>> callback) {
    List<String> emptyFieldNames = new ArrayList<>();
    if (StringUtils.isEmpty(this.className)) {
      emptyFieldNames.add("className");
    }
    if (this.status == null) {
      emptyFieldNames.add("status");
    }
    if (emptyFieldNames.size() > 0) {
      String message = String.format("%s cannot be empty", StringUtils.joinWith(", ", emptyFieldNames.toArray()));
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder().message(message).build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder().error(Collections.singletonList(errorMessage)).build();
      return CompletableFuture.completedFuture(new Result<>(HttpStatus.SC_BAD_REQUEST, null, errors));
    }

    CompletableFuture<Result<ShardStatus[]>> updateAll = CompletableFuture.supplyAsync(() -> {
      try {
        Result<Shard[]> shards = this.shardsGetter.withClassName(this.className).run().get();
        if (shards.hasErrors()) {
          return shards.<ShardStatus[]>toErrorResult();
        }

        List<ShardStatus> shardStatuses = new ArrayList<>();
        for (Shard shard : shards.getResult()) {
          Result<ShardStatus> update = this.shardUpdater
            .withClassName(this.className)
            .withShardName(shard.getName())
            .withStatus(this.status).run().get();
          if (update.hasErrors()) {
            return update.<ShardStatus[]>toErrorResult();
          }
          shardStatuses.add(update.getResult());
        }

        return new Result<ShardStatus[]>(HttpStatus.SC_OK, shardStatuses.toArray(new ShardStatus[shardStatuses.size()]), null);
      } catch (ExecutionException | InterruptedException e) {
        throw new CompletionException(e);
      }
    });

    if (callback == null) {
      return updateAll;
    }
    return updateAll.whenComplete((statuses, e) -> {
      callback.completed(statuses);
      if (e != null) {
        callback.failed(new Exception(e));
      }
    });
  }
}
