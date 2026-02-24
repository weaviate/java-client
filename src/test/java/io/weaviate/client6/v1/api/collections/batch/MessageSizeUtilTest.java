package io.weaviate.client6.v1.api.collections.batch;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

public class MessageSizeUtilTest {
  /** Derive the value of {@code SAFETY_MARGIN} from the public API. */
  private static int DERIVED_SAFETY_MARGIN = 4096 - MessageSizeUtil.maxSizeBytes(4096);

  private static WeaviateProtoBatch.BatchObject OBJECT = WeaviateProtoBatch.BatchObject.newBuilder()
      .setUuid(UUID.randomUUID().toString())
      .setCollection("Test")
      .build();
  private static WeaviateProtoBatch.BatchReference REFERENCE = WeaviateProtoBatch.BatchReference.newBuilder()
      .setFromCollection("From")
      .setFromUuid(UUID.randomUUID().toString())
      .build();

  @Test
  public void test_safety_margin() {
    int got = 0;

    var objects = WeaviateProtoBatch.BatchStreamRequest.Data.Objects.newBuilder();
    for (int i = 0; i < 10_000; i++) {
      objects.addValues(OBJECT);
      got += MessageSizeUtil.ofDataField(OBJECT, Data.Type.OBJECT);
    }

    var refernces = WeaviateProtoBatch.BatchStreamRequest.Data.References.newBuilder();
    for (int i = 0; i < 10_000; i++) {
      refernces.addValues(REFERENCE);
      got += MessageSizeUtil.ofDataField(REFERENCE, Data.Type.REFERENCE);
    }

    var request = WeaviateProtoBatch.BatchStreamRequest.newBuilder()
        .setData(WeaviateProtoBatch.BatchStreamRequest.Data.newBuilder()
            .setObjects(objects)
            .setReferences(refernces)
            .build())
        .build();

    int want = request.getSerializedSize();
    Assertions.assertThat(got).isLessThan(want);
    Assertions.assertThat(want - got).isLessThan(DERIVED_SAFETY_MARGIN);
  }

  @Test
  public void test_maxSizeBytes_legal() {
    int sizeBytes = 4096;
    int want = sizeBytes - DERIVED_SAFETY_MARGIN;
    int got = MessageSizeUtil.maxSizeBytes(sizeBytes);
    Assertions.assertThat(got).isEqualTo(want);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_maxSizeBytes_illegal() {
    MessageSizeUtil.maxSizeBytes(DERIVED_SAFETY_MARGIN);
  }
}
