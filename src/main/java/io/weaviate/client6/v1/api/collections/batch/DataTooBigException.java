package io.weaviate.client6.v1.api.collections.batch;

public class DataTooBigException extends Exception {
  DataTooBigException(Data data, long maxSizeBytes) {
    super("%s with size=%dB exceeds maximum message size %dB".formatted(data, data.sizeBytes(), maxSizeBytes));
  }
}
