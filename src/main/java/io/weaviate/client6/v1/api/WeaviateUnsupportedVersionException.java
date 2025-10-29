package io.weaviate.client6.v1.api;

import io.weaviate.client6.v1.internal.VersionSupport;

/**
 * This exception is thrown when the client refuses to talk to an unsupported
 * version of the server, see {@link VersionSupport#MINIMAL_SUPPORTED_VERSION}.
 */
public class WeaviateUnsupportedVersionException extends WeaviateException {
  public WeaviateUnsupportedVersionException(String actual) {
    this(VersionSupport.MINIMAL_SUPPORTED_VERSION.toString(), actual);
  }

  public WeaviateUnsupportedVersionException(String minimal, String actual) {
    super("Server version %s is not supported. Earliest supported version is %s.".formatted(actual, minimal));
  }
}
