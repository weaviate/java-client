package io.weaviate.client.v1.async.aliases.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.aliases.model.Alias;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class AliasAllGetter extends AsyncBaseClient<Map<String, Alias>>
    implements AsyncClientResult<Map<String, Alias>> {

  public AliasAllGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  @Override
  public Future<Result<Map<String, Alias>>> run(FutureCallback<Result<Map<String, Alias>>> callback) {
    return sendGetRequest("/aliases", callback, new ResponseParser<Map<String, Alias>>() {

      class ResponseBody {
        List<Alias> aliases;
      }

      @Override
      public Result<Map<String, Alias>> parse(HttpResponse response, String body, ContentType contentType) {
        Response<ResponseBody> resp = serializer.toResponse(response.getCode(), body, ResponseBody.class);
        if (resp.getErrors() != null) {
          return new Result<>(resp, null);
        }
        Map<String, Alias> aliases = resp.getBody().aliases.stream()
            .collect(Collectors.toMap(Alias::getAlias, Function.identity()));
        return new Result<>(resp, aliases);
      }
    });
  }
}
