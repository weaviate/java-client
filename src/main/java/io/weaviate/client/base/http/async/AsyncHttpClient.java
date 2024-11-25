package io.weaviate.client.base.http.async;

import io.weaviate.client.Config;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public class AsyncHttpClient {

  public static CloseableHttpAsyncClient create(Config config) {
    PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
      .setTlsStrategy(ClientTlsStrategyBuilder.create()
        .setSslContext(SSLContexts.createSystemDefault())
        .setTlsVersions(TLS.V_1_3)
        .build())
      .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
      .setConnPoolPolicy(PoolReusePolicy.LIFO)
      .setDefaultConnectionConfig(ConnectionConfig.custom()
        .setSocketTimeout(Timeout.ofSeconds(config.getSocketTimeout()))
        .setConnectTimeout(Timeout.ofSeconds(config.getConnectionTimeout()))
        .setTimeToLive(TimeValue.ofMinutes(10))
        .build())
      .setDefaultTlsConfig(TlsConfig.custom()
        .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
        .setHandshakeTimeout(Timeout.ofMinutes(1))
        .build())
      .build();
    IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
      .setSoTimeout(Timeout.ofSeconds(config.getSocketTimeout()))
      .build();

    return HttpAsyncClients.custom()
      .setConnectionManager(connectionManager)
      .setIOReactorConfig(ioReactorConfig)
      .build();
  }
}
