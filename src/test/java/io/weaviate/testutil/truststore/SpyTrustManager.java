package io.weaviate.testutil.truststore;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Test fixture that records when this TrustManager has been used.
 * Combine with {@link SingleTrustManagerFactory#create} to mock
 * a custom TrustStore.
 */
public class SpyTrustManager implements X509TrustManager {
  private boolean used = false;

  public boolean wasUsed() {
    return this.used;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    this.used = true;
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    this.used = true;
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    this.used = true;
    return new X509Certificate[0];
  }

  public static Optional<SpyTrustManager> getSpy(TrustManagerFactory tmf) {
    var managers = tmf.getTrustManagers();
    if (managers.length == 0) {
      return Optional.empty();
    }
    return Optional.of((SpyTrustManager) managers[0]);
  }
}
