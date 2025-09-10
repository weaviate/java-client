package io.weaviate.client.v1.groups.api.oidc;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.aliases.model.Alias;

public class KnownGroupNamesGetter extends BaseClient<String[]> implements ClientResult<List<String>> {

  public KnownGroupNamesGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  static class ResponseBody {
    List<Alias> aliases;
  }

  @Override
  public Result<List<String>> run() {
    return Result.toList(sendGetRequest("/authz/groups/oidc", String[].class));
  }
}
