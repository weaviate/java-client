package io.weaviate.client.base.http;

public interface HttpClient {
  HttpResponse sendGetRequest(String url) throws Exception;
  HttpResponse sendPostRequest(String url, String json) throws Exception;
  HttpResponse sendPutRequest(String url, String json) throws Exception;
  HttpResponse sendPatchRequest(String url, String json) throws Exception;
  HttpResponse sendDeleteRequest(String url, String json) throws Exception;
  HttpResponse sendHeadRequest(String url) throws Exception;
}
