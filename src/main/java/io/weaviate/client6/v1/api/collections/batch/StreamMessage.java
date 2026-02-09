package io.weaviate.client6.v1.api.collections.batch;

import java.util.Optional;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

@FunctionalInterface
interface StreamMessage {
  void appendTo(WeaviateProtoBatch.BatchStreamRequest.Builder builder);

  static StreamMessage start(Optional<ConsistencyLevel> consistencyLevel) {
    final WeaviateProtoBatch.BatchStreamRequest.Start.Builder start = WeaviateProtoBatch.BatchStreamRequest.Start
        .newBuilder();
    consistencyLevel.ifPresent(value -> value.appendTo(start));
    return builder -> builder.setStart(start);
  }

  static final StreamMessage STOP = builder -> builder
      .setStop(WeaviateProtoBatch.BatchStreamRequest.Stop.getDefaultInstance());

  static StreamMessage stop() {
    return STOP;
  }
}
