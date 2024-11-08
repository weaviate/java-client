package io.weaviate.client.base.http.async;

import io.weaviate.client.Config;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

public class AsyncHttpClient {

  public static CloseableHttpAsyncClient create(Config config) {
    IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
      .setSoTimeout(Timeout.ofSeconds(config.getSocketTimeout()))
      .build();

    return HttpAsyncClients.custom()
      .setIOReactorConfig(ioReactorConfig)
      .build();
  }
}
