package technology.semi.weaviate.client.base.http.impl;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
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
  public HttpResponse sendDeleteRequest(String url) throws Exception {
    HttpDelete httpDelete = new HttpDelete(url);
    httpDelete.setHeader("Accept", "*/*");
    return sendRequest(httpDelete);
  }

  private HttpResponse sendPayloadRequest(String url, String jsonString, String method) throws Exception {
    StringEntity entity = new StringEntity(jsonString);
    HttpEntityEnclosingRequestBase httpPost = getRequest(url, method);
    httpPost.setEntity(entity);
    httpPost.setHeader("Accept", "*/*");
    httpPost.setHeader("Content-type", "application/json");
    return sendRequest(httpPost);
  }


  private HttpEntityEnclosingRequestBase getRequest(String url, String method) {
    if (method == "PUT") {
      return new HttpPut(url);
    } else if (method == "PATCH") {
      return new HttpPatch(url);
    } else {
      return new HttpPost(url);
    }
  }

  private HttpResponse sendRequest(HttpUriRequest request) throws Exception {
    int statusCode = 0;
    CloseableHttpClient client = HttpClients.createDefault();
    CloseableHttpResponse response = client.execute(request);

    statusCode = response.getStatusLine().getStatusCode();
    String bodyAsString = (response.getEntity() != null) ? EntityUtils.toString(response.getEntity()) : "";
    client.close();

    return new HttpResponse(statusCode, bodyAsString);
  }
}
