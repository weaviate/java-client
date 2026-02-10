package io.weaviate.client6.v1.api.collections.batch;

/**
 * DataTooBigException is thrown when a single object exceeds
 * the maximum size of a gRPC message.
 */
public class DataTooBigException extends Exception {
  DataTooBigException(Data data, long maxSizeBytes) {
    super("%s with size=%dB exceeds maximum message size %dB".formatted(data, data.sizeBytes(), maxSizeBytes));
  }
}
