package io.weaviate.client.base.http.impl;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.http.HttpResponse;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

public class CommonsHttpClientImpl implements HttpClient, Closeable {
  private final Map<String, String> headers;
  private AccessTokenProvider tokenProvider;
  private final CloseableHttpClientBuilder clientBuilder;

  public CommonsHttpClientImpl(Map<String, String> headers, CloseableHttpClientBuilder clientBuilder) {
    this(headers, null, clientBuilder);
  }

  public CommonsHttpClientImpl(Map<String, String> headers, AccessTokenProvider tokenProvider, CloseableHttpClientBuilder clientBuilder) {
    this.headers = headers;
    this.clientBuilder = clientBuilder;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public HttpResponse sendGetRequest(String url) throws Exception {
    return sendRequestWithoutPayload(new HttpGet(url));
  }

  @Override
  public HttpResponse sendPostRequest(String url, String json) throws Exception {
    return sendRequestWithPayload(new HttpPost(url), json);
  }

  @Override
  public HttpResponse sendPutRequest(String url, String json) throws Exception {
    return sendRequestWithPayload(new HttpPut(url), json);
  }

  @Override
  public HttpResponse sendPatchRequest(String url, String json) throws Exception {
    return sendRequestWithPayload(new HttpPatch(url), json);
  }

  @Override
  public HttpResponse sendDeleteRequest(String url, String json) throws Exception {
    if (json == null) {
      return sendRequestWithoutPayload(new HttpDelete(url));
    }
    return sendRequestWithPayload(new HttpDeleteWithBody(url), json);
  }

  @Override
  public HttpResponse sendHeadRequest(String url) throws Exception {
    return sendRequestWithoutPayload(new HttpHead(url));
  }

  private HttpResponse sendRequestWithoutPayload(BasicClassicHttpRequest request) throws Exception {
    request.setHeader(HttpHeaders.ACCEPT, "*/*");
    return sendRequest(request);
  }

  private HttpResponse sendRequestWithPayload(BasicClassicHttpRequest request, String jsonString) throws Exception {
    request.setHeader(HttpHeaders.ACCEPT, "application/json");
    request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    request.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
    return sendRequest(request);
  }

  private HttpResponse sendRequest(BasicClassicHttpRequest request) throws Exception {
    if (headers != null && headers.size() > 0) {
      headers.forEach(request::addHeader);
    }
    if (tokenProvider != null) {
      request.addHeader("Authorization", String.format("Bearer %s", tokenProvider.getAccessToken()));
    }

    CloseableHttpClient client = clientBuilder.build();
    CloseableHttpResponse response = client.execute(request);

    int statusCode = response.getCode();
    String body = response.getEntity() != null
      ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
      : "";
    client.close();

    return new HttpResponse(statusCode, body);
  }

  @Override
  public void close() throws IOException {
    if (tokenProvider != null) {
      tokenProvider.shutdown();
    }
  }

  private static class HttpDeleteWithBody extends HttpUriRequestBase {
    public HttpDeleteWithBody(String url) {
      super(HttpDelete.METHOD_NAME, URI.create(url));
    }
  }

  public interface CloseableHttpClientBuilder {
    CloseableHttpClient build();
  }
}
