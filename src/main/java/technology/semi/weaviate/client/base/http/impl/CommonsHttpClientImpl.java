package technology.semi.weaviate.client.base.http.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.http.HttpResponse;

public class CommonsHttpClientImpl implements HttpClient {
  private final Map<String, String> headers;

  public CommonsHttpClientImpl(Map<String, String> headers) {
    this.headers = headers;
  }

  private class HttpDeleteWithBody extends HttpPost {
    public HttpDeleteWithBody(String url) {
      super(url);
    }

    @Override
    public String getMethod() {
      return "DELETE";
    }
  }

  @Override
  public HttpResponse sendGetRequest(String url) throws Exception {
    HttpGet httpGet = new HttpGet(url);
    httpGet.setHeader("Accept", "*/*");
    return sendRequest(httpGet);
  }

  @Override
  public HttpResponse sendPostRequest(String url, String json) throws Exception {
    return sendPayloadRequest(url, json, "POST");
  }

  @Override
  public HttpResponse sendPutRequest(String url, String json) throws Exception {
    return sendPayloadRequest(url, json, "PUT");
  }

  @Override
  public HttpResponse sendPatchRequest(String url, String json) throws Exception {
    return sendPayloadRequest(url, json, "PATCH");
  }

  @Override
  public HttpResponse sendDeleteRequest(String url, String json) throws Exception {
    if (json == null) {
      HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
      httpDelete.setHeader("Accept", "application/json");
      if (headers != null && headers.size() > 0) {
        headers.forEach(httpDelete::addHeader);
      }
      return sendRequest(httpDelete);
    }
    return sendPayloadRequest(url, json, "DELETE");
  }

  private HttpResponse sendPayloadRequest(String url, String jsonString, String method) throws Exception {
    StringEntity entity = new StringEntity(jsonString, StandardCharsets.UTF_8);
    HttpEntityEnclosingRequestBase httpPost = getRequest(url, method);
    httpPost.setEntity(entity);
    httpPost.setHeader("Accept", "application/json");
    httpPost.setHeader("Content-type", "application/json");
    return sendRequest(httpPost);
  }

  private HttpEntityEnclosingRequestBase getRequest(String url, String method) {
    HttpEntityEnclosingRequestBase request = createRequest(url, method);
    if (headers != null && headers.size() > 0) {
      headers.forEach(request::addHeader);
    }
    return request;
  }

  private HttpEntityEnclosingRequestBase createRequest(String url, String method) {
    if (Objects.equals(method, "PUT")) {
      return new HttpPut(url);
    } else if (Objects.equals(method, "PATCH")) {
      return new HttpPatch(url);
    } else if (Objects.equals(method, "DELETE")) {
      return new HttpDeleteWithBody(url);
    } else {
      return new HttpPost(url);
    }
  }

  private HttpResponse sendRequest(HttpUriRequest request) throws Exception {
    int statusCode = 0;
    CloseableHttpClient client = HttpClients.createDefault();
    CloseableHttpResponse response = client.execute(request);

    statusCode = response.getStatusLine().getStatusCode();
    String bodyAsString = (response.getEntity() != null) ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";
    client.close();

    return new HttpResponse(statusCode, bodyAsString);
  }
}
