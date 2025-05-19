package io.weaviate.client6.v1.internal.grpc;

import java.util.Collection;
import java.util.Map;

public interface GrpcChannelOptions {
  String host();

  Collection<Map.Entry<String, String>> headers();

  boolean useTls();
}
