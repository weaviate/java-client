package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.ObjectReference;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

@Immutable
@SuppressWarnings("deprecation") // protoc uses GeneratedMessageV3
class Data implements Message {

  /**
   * Raw input value, as passed by the user.
   */
  private final Object raw;

  /**
   * Task ID. Depending on the underlying object, this will either be
   * {@link WeaviateObject#uuid} or {@link ObjectReference#beacon}.
   *
   * Since UUIDs and beacons cannot clash, ID does not encode any
   * information about the underlying data type.
   */
  private final String id;

  /**
   * Serialized representation of the {@link #raw}. This valus is immutable
   * for the entire lifecycle of the handle.
   */
  private final GeneratedMessage.ExtendableMessage<GeneratedMessageV3> message;

  /** Estimated size of the {@link #message} when serialized. */
  private final int sizeBytes;

  enum Type {
    OBJECT(WeaviateProtoBatch.BatchStreamRequest.Data.OBJECTS_FIELD_NUMBER),
    REFERENCE(WeaviateProtoBatch.BatchStreamRequest.Data.REFERENCES_FIELD_NUMBER);

    private final int fieldNumber;

    private Type(int fieldNumber) {
      this.fieldNumber = fieldNumber;
    }

    public int fieldNumber() {
      return fieldNumber;
    }
  }

  private Data(Object raw, String id, GeneratedMessage.ExtendableMessage<GeneratedMessageV3> message, int sizeBytes) {
    this.raw = requireNonNull(raw, "raw is null");
    this.id = requireNonNull(id, "id is null");
    this.message = requireNonNull(message, "message is null");

    assert sizeBytes >= 0;
    this.sizeBytes = sizeBytes;
  }

  Data(Object raw, String id, GeneratedMessage.ExtendableMessage<GeneratedMessageV3> message,
      Type type) {
    this(raw, id, message, MessageSizeUtil.ofDataField(message, type));
  }

  String id() {
    return id;
  }

  /** Serialized data size in bytes. */
  int sizeBytes() {
    return sizeBytes;
  }

  @Override
  public void appendTo(WeaviateProtoBatch.BatchStreamRequest.Builder builder) {
    WeaviateProtoBatch.BatchStreamRequest.Data.Builder data = requireNonNull(builder, "builder is null")
        .getDataBuilder();
    if (message instanceof WeaviateProtoBatch.BatchObject object) {
      data.getObjectsBuilder().addValues(object);
    } else if (message instanceof WeaviateProtoBatch.BatchReference ref) {
      data.getReferencesBuilder().addValues(ref);
    }
  }

  @Override
  public String toString() {
    return "%s (%s)".formatted(raw.getClass().getSimpleName(), id);
  }
}
