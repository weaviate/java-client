package io.weaviate.client.base.http.builder;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.impl.CommonsHttpClientImpl;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;

public class HttpApacheClientBuilder {

  private HttpApacheClientBuilder() {}

  public static CommonsHttpClientImpl.CloseableHttpClientBuilder build(Config config) {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
      .setConnectTimeout(Timeout.of(config.getConnectionTimeout(), TimeUnit.SECONDS))
      .setConnectionRequestTimeout(Timeout.of(config.getConnectionRequestTimeout(), TimeUnit.SECONDS))
      .setResponseTimeout(Timeout.of(config.getSocketTimeout(), TimeUnit.SECONDS));

    if (config.getProxyHost() != null) {
      requestConfigBuilder.setProxy(new HttpHost(config.getProxyScheme(), config.getProxyHost(), config.getProxyPort()));
    }

    RequestConfig requestConfig = requestConfigBuilder.build();
    return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)::build;
  }
}
