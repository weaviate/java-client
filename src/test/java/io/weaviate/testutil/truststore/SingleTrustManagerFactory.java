package io.weaviate.testutil.truststore;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

/** TrustManagerFactory which always returns the same {@code TrustManager}. */
public final class SingleTrustManagerFactory extends TrustManagerFactory {

  /** Create a factory that will return {@code TrustManager tm}. */
  public static TrustManagerFactory create(TrustManager tm) {
    return new SingleTrustManagerFactory(tm);
  }

  protected SingleTrustManagerFactory(TrustManager tm) {
    super(new SingleTrustManagerFactorySpi(tm), null, TrustManagerFactory.getDefaultAlgorithm());
  }

  /**
   * Naive {@code TrustManagerFactorySpi} implementation
   * which always returns the same {@code TrustManager}.
   */
  private static final class SingleTrustManagerFactorySpi extends TrustManagerFactorySpi {
    private final TrustManager[] trustManagers;

    private SingleTrustManagerFactorySpi(TrustManager tm) {
      this.trustManagers = new TrustManager[] { tm };
    }

    @Override
    protected void engineInit(KeyStore ks) throws KeyStoreException {
      return;
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
      return;
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
      return trustManagers;
    }
  }
}
