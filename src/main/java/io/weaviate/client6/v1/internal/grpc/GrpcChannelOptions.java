package io.weaviate.client6.v1.internal.grpc;

import java.util.Collections;
import java.util.Map;

// TODO: unify with rest.TransportOptions?
public interface GrpcChannelOptions {
  String host();

  default Map<String, String> headers() {
    return Collections.emptyMap();
  }

  boolean useTls();
}
