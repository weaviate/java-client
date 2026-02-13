package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import io.weaviate.client6.v1.api.WeaviateException;

/**
 * DataTooBigException is thrown when a single object exceeds
 * the maximum size of a gRPC message.
 */
public class DataTooBigException extends WeaviateException {
  DataTooBigException(Data data, long maxSizeBytes) {
    super("%s with size=%dB exceeds maximum message size %dB".formatted(
        requireNonNull(data, "data is null"), data.sizeBytes(), maxSizeBytes));
  }
}
