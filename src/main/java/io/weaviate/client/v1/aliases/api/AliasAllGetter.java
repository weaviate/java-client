package io.weaviate.client.v1.aliases.api;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.aliases.api.AliasAllGetter.ResponseBody;
import io.weaviate.client.v1.aliases.model.Alias;

public class AliasAllGetter extends BaseClient<ResponseBody> implements ClientResult<Map<String, Alias>> {
  public AliasAllGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  static class ResponseBody {
    List<Alias> aliases;
  }

  @Override
  public Result<Map<String, Alias>> run() {
    Response<ResponseBody> resp = sendGetRequest("/aliases", ResponseBody.class);
    if (resp.getErrors() != null) {
      return new Result<>(resp, null);
    }
    Map<String, Alias> aliases = resp.getBody().aliases.stream()
        .collect(Collectors.toMap(Alias::getAlias, Function.identity()));
    return new Result<>(resp, aliases);
  }
}
