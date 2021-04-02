package technology.semi.weaviate.client.base;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import technology.semi.weaviate.client.Config;

public abstract class BaseClient<T> {
  private Config config;

  public BaseClient(Config config) {
    this.config = config;
  }

  protected Response<T> sendGetRequest(String request, Class<T> classOfT) {
    return sendRequest(request, "GET", classOfT);
  }

  private Response<T> sendRequest(String request, String method, Class<T> classOfT) {
    try {
      URL url = getRequest(request);
      HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setRequestMethod(method);
      httpConn.setUseCaches(false);

      httpConn.setDoInput(true); // true if we want to read server's response
      httpConn.setDoOutput(false);
      httpConn.setRequestProperty("Accept", "*/*");

      InputStream inputStream = httpConn.getInputStream();
      int statusCode = httpConn.getResponseCode();
      T body = toResponse(inputStream, classOfT);

      return new Response<T>(statusCode, body);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private URL getRequest(String request) throws MalformedURLException {
    return new URL(this.config.getBaseURL() + request);
  }

  private T toResponse(InputStream inputStream, Class<T> classOfT) {
    return new Gson().fromJson(new InputStreamReader(inputStream), classOfT);
  }
}
