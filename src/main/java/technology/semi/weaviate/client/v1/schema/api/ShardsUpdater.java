package technology.semi.weaviate.client.v1.schema.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateError;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;
import technology.semi.weaviate.client.v1.schema.model.Shard;
import technology.semi.weaviate.client.v1.schema.model.ShardStatus;

public class ShardsUpdater implements ClientResult<ShardStatus[]> {
  private final ShardsGetter shardsGetter;
  private final ShardUpdater shardUpdater;

  private String className;
  private String status;

  public ShardsUpdater(Config config) {
    this.shardsGetter = new ShardsGetter(config);
    this.shardUpdater = new ShardUpdater(config);
  }

  public ShardsUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ShardsUpdater withStatus(String targetStatus) {
    this.status = targetStatus;
    return this;
  }

  private Result<ShardStatus[]> toResult(WeaviateError error) {
    return new Result<>(error.getStatusCode(), null, WeaviateErrorResponse.builder().error(error.getMessages()).build());
  }

  @Override
  public Result<ShardStatus[]> run() {
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
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Collections.singletonList(errorMessage)).build();
      return new Result<>(HttpStatus.SC_BAD_REQUEST, null, errors);
    }

    Result<Shard[]> shards = this.shardsGetter.withClassName(this.className).run();
    if (shards.hasErrors()) {
      return toResult(shards.getError());
    }

    List<ShardStatus> shardStatuses = new ArrayList<>();
    for (Shard shard : shards.getResult()) {
      Result<ShardStatus> update = this.shardUpdater
        .withClassName(this.className)
        .withShardName(shard.getName())
        .withStatus(this.status)
        .run();
      if (update.hasErrors()) {
        return toResult(update.getError());
      }
      shardStatuses.add(update.getResult());
    }

    return new Result<>(HttpStatus.SC_OK, shardStatuses.toArray(new ShardStatus[shardStatuses.size()]), null);
  }
}
