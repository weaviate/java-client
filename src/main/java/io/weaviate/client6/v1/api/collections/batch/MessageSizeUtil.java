package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

final class MessageSizeUtil {
  /**
   * Safety margin of 1KB to allow for the overhead of surrounding Data field tags
   * and the encoded length of the final payload.
   *
   * @apiNote Package-private for testing.
   */
  static final int SAFETY_MARGIN = 1024;

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
  static int maxSizeBytes(int maxSizeBytes) {
    if (maxSizeBytes <= SAFETY_MARGIN) {
      throw new IllegalArgumentException("Maximum batch size must be at least %dB".formatted(SAFETY_MARGIN));
    }
    return maxSizeBytes - SAFETY_MARGIN;
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
