package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

final class MessageSizeUtil {
  private static int DATA_TAG_SIZE = CodedOutputStream
      .computeTagSize(WeaviateProtoBatch.BatchStreamRequest.DATA_FIELD_NUMBER);

  private MessageSizeUtil() {
  }

  /**
   * Adjust batch byte-size limit to account for the
   * {@link WeaviateProtoBatch.BatchStreamRequest.Data} container.
   *
   * <p>
   * A protobuf field has layout {@code [tag][lenght(payload)][payload]},
   * so to estimate the batch size correctly we must account for "tag"
   * and "length", not just the raw payload.
   */
  static long maxSizeBytes(long maxSizeBytes) {
    if (maxSizeBytes <= DATA_TAG_SIZE) {
      throw new IllegalArgumentException("Maximum batch size must be at least %dB".formatted(DATA_TAG_SIZE));
    }
    return maxSizeBytes - DATA_TAG_SIZE;
  }

  /**
   * Calculate the size of a serialized
   * {@link WeaviateProtoBatch.BatchStreamRequest.Data} field.
   */
  @SuppressWarnings("deprecation") // protoc uses GeneratedMessageV3
  static int ofDataField(GeneratedMessage.ExtendableMessage<GeneratedMessageV3> message, Data.Type type) {
    requireNonNull(type, "type is null");
    requireNonNull(message, "message is null");
    return CodedOutputStream.computeMessageSize(type.fieldNumber(), message);
  }
}
