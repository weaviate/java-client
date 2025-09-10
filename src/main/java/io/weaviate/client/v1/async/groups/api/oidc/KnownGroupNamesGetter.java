package io.weaviate.client.v1.async.groups.api.oidc;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.aliases.model.Alias;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class KnownGroupNamesGetter extends AsyncBaseClient<List<String>> implements AsyncClientResult<List<String>> {

  public KnownGroupNamesGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  static class ResponseBody {
    List<Alias> aliases;
  }

  @Override
  public Future<Result<List<String>>> run(FutureCallback<Result<List<String>>> callback) {
    return sendGetRequest("/authz/groups/oidc", callback, Result.arrayToListParser(String[].class));
  }
}
