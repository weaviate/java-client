package io.weaviate.client.base.http.builder;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import io.weaviate.client.Config;
import io.weaviate.client.base.http.impl.CommonsHttpClientImpl;

public class HttpApacheClientBuilder {

  private HttpApacheClientBuilder() {}

  public static CommonsHttpClientImpl.CloseableHttpClientBuilder build(Config config) {
    RequestConfig requestConfig = RequestConfig.custom()
      .setConnectTimeout(config.getConnectionTimeout() * 1000)
      .setConnectionRequestTimeout(config.getConnectionRequestTimeout() * 1000)
      .setSocketTimeout(config.getSocketTimeout() * 1000).build();
    return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)::build;
  }
}
