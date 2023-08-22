package io.weaviate.client.base.http.builder;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import io.weaviate.client.Config;
import io.weaviate.client.base.http.impl.CommonsHttpClientImpl;

public class HttpApacheClientBuilder {

  private HttpApacheClientBuilder() {}

  public static CommonsHttpClientImpl.CloseableHttpClientBuilder build(Config config) {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
      .setConnectTimeout(config.getConnectionTimeout() * 1000)
      .setConnectionRequestTimeout(config.getConnectionRequestTimeout() * 1000)
      .setSocketTimeout(config.getSocketTimeout() * 1000);
      
    if (config.getProxyUrl() != null) {
      requestConfigBuilder.setProxy(new HttpHost(config.getProxyUrl(), config.getProxyPort(), config.getProxyScheme()));
    }

    RequestConfig requestConfig = requestConfigBuilder.build();
    return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)::build;
  }
}
