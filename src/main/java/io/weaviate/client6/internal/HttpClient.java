package io.weaviate.client6.internal;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class HttpClient implements Closeable {
  // TODO: move somewhere
  // public static final Gson GSON =

  public final CloseableHttpClient http;

  public HttpClient() {
    http = HttpClients.createDefault();
  }

  @Override
  public void close() throws IOException {
    http.close();
  }
}
