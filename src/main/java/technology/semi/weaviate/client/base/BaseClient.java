package technology.semi.weaviate.client.base;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import technology.semi.weaviate.client.Config;

public abstract class BaseClient<T> {
  private Config config;

  public BaseClient(Config config) {
    this.config = config;
  }

  protected Response<T> sendGetRequest(String endpoint, Class<T> classOfT) {
    String address = config.getBaseURL() + endpoint;
    Request request = new Request.Builder()
            .url(address)
            .addHeader("Accept", "*/*")
            .build();
    return sendRequest(request, classOfT);
  }

  protected Response<T> sendPostRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendPayloadRequest(endpoint, "POST", payload, classOfT);
  }

  protected Response<T> sendPutRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendPayloadRequest(endpoint, "PUT", payload, classOfT);
  }

  protected Response<T> sendPatchRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendPayloadRequest(endpoint, "PATCH", payload, classOfT);
  }

  protected Response<T> sendPayloadRequest(String endpoint, String method, Object payload, Class<T> classOfT) {
    String address = config.getBaseURL() + endpoint;
    RequestBody body = RequestBody.create(toJsonString(payload), MediaType.parse("application/json"));
    Request.Builder builder = new Request.Builder()
            .url(address)
            .addHeader("Content-Type", "json")
            .addHeader("Accept", "*/*");
    if (method == "PUT") {
      builder = builder.put(body);
    } else if (method == "PATCH") {
      builder = builder.patch(body);
    } else {
      builder = builder.post(body);
    }
    return sendRequest(builder.build(), classOfT);
  }

  protected Response<T> sendDeleteRequest(String endpoint, Class<T> classOfT) {
    String address = config.getBaseURL() + endpoint;
    Request request = new Request.Builder()
            .url(address)
            .addHeader("Accept", "*/*")
            .delete()
            .build();
    return sendRequest(request, classOfT);
  }

  private Response<T> sendRequest(Request request, Class<T> classOfT) {
    try {
      OkHttpClient httpClient = new OkHttpClient();

      okhttp3.Response response = httpClient.newCall(request).execute();

      int statusCode = response.code();
      InputStream inputStream = response.body().byteStream();

      T body = toResponse(inputStream, classOfT);

      return new Response<T>(statusCode, body);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private T toResponse(InputStream inputStream, Class<T> classOfT) {
    return new Gson().fromJson(new BufferedReader(new InputStreamReader(inputStream)), classOfT);
  }

  private String toJsonString(Object object) {
    return new Gson().toJson(object);
  }
}
