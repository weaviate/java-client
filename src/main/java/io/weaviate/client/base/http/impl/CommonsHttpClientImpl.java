package io.weaviate.client.base.http.impl;

import java.io.Closeable;
import java.io.IOException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.http.HttpResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

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

  private HttpResponse sendRequestWithoutPayload(HttpRequestBase request) throws Exception {
    request.setHeader(HttpHeaders.ACCEPT, "*/*");
    return sendRequest(request);
  }

  private HttpResponse sendRequestWithPayload(HttpEntityEnclosingRequestBase request, String jsonString) throws Exception {
    request.setHeader(HttpHeaders.ACCEPT, "application/json");
    request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    request.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
    return sendRequest(request);
  }

  private HttpResponse sendRequest(HttpUriRequest request) throws Exception {
    if (headers != null && headers.size() > 0) {
      headers.forEach(request::addHeader);
    }
    if (tokenProvider != null) {
      request.addHeader("Authorization", String.format("Bearer %s", tokenProvider.getAccessToken()));
    }

    CloseableHttpClient client = clientBuilder.build();
    CloseableHttpResponse response = client.execute(request);

    int statusCode = response.getStatusLine().getStatusCode();
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

  private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public HttpDeleteWithBody() {
    }

    public HttpDeleteWithBody(URI uri) {
      this.setURI(uri);
    }

    public HttpDeleteWithBody(String uri) {
      this.setURI(URI.create(uri));
    }

    public String getMethod() {
      return HttpDelete.METHOD_NAME;
    }
  }



  public interface CloseableHttpClientBuilder {
    CloseableHttpClient build();
  }
}
