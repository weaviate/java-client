package technology.semi.weaviate.client.v1.schema.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;
import technology.semi.weaviate.client.v1.schema.model.ShardStatus;

public class ShardUpdater extends BaseClient<ShardStatus> implements ClientResult<ShardStatus> {
  private String className;
  private String shardName;
  private ShardStatus status;

  public ShardUpdater(Config config) {
    super(config);
  }

  public ShardUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ShardUpdater withShardName(String shardName) {
    this.shardName = shardName;
    return this;
  }

  public ShardUpdater withStatus(String targetStatus) {
    this.status = ShardStatus.builder().status(targetStatus).build();
    return this;
  }

  @Override
  public Result<ShardStatus> run() {
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
      return new Result<>(HttpStatus.SC_BAD_REQUEST, null, errors);
    }
    String path = String.format("/schema/%s/shards/%s", this.className, this.shardName);
    Response<ShardStatus> resp = sendPutRequest(path, status, ShardStatus.class);
    return new Result<>(resp);
  }
}
