package io.weaviate.client6.v1.internal.rest;

import java.util.Collections;
import java.util.Map;

public interface TransportOptions {
  String host();

  default Map<String, String> headers() {
    return Collections.emptyMap();
  }
}
