package io.weaviate.client6.v1.api;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public class InstanceMetadataRequest {
  public static final Endpoint<Void, InstanceMetadata> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      __ -> "/meta",
      __ -> Collections.emptyMap(),
      InstanceMetadata.class);
}
