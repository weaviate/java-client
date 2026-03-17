package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

@FunctionalInterface
interface Message {
  void appendTo(WeaviateProtoBatch.BatchStreamRequest.Builder builder);

  /** Create a Start message. */
  static Message start(Optional<ConsistencyLevel> consistencyLevel) {
    requireNonNull(consistencyLevel, "consistencyLevel is null");

    final WeaviateProtoBatch.BatchStreamRequest.Start.Builder start = WeaviateProtoBatch.BatchStreamRequest.Start
        .newBuilder();
    consistencyLevel.ifPresent(value -> value.appendTo(start));
    return builder -> builder.setStart(start);
  }

  /** Create a Stop message. */
  static Message stop() {
    return STOP;
  }

  static final Message STOP = builder -> builder
      .setStop(WeaviateProtoBatch.BatchStreamRequest.Stop.getDefaultInstance());

}
