package io.weaviate.client6.v1.api.collections.batch;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.InsertManyRequest;
import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class BatchTest {

  private static final int OBJECT_SIZE_BYTES = MessageSizeUtil.ofDataField(
      newBatchObject(WeaviateObject.of(o -> o.uuid(UUID.randomUUID().toString()))),
      Data.Type.OBJECT);

  @Test
  public void test_isEmpty() {
    Batch batch = new Batch(10, 4096);
    Assertions.assertThat(batch.isEmpty()).as("batch is empty").isTrue();
  }

  @Test
  public void test_isFull_sizeBytes() {
    // Batch is bottlenecked by maxSizeBytes and will be full with 3 objects.
    Batch batch = new Batch(10, 3 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);

    addObject(batch);

    Assertions.assertThat(batch.isEmpty()).as("batch is empty").isFalse();
    Assertions.assertThat(batch.isFull()).as("batch is full").isFalse();

    addObject(batch);

    Assertions.assertThat(batch.isEmpty()).as("batch is empty").isFalse();
    Assertions.assertThat(batch.isFull()).as("batch is full").isFalse();

    addObject(batch);

    Assertions.assertThat(batch.isFull()).as("batch is full").isTrue();
  }

  @Test
  public void test_isFull_size() {
    // Batch is bottlenecked by maxSize and will be full with 2 objects.
    Batch batch = new Batch(2, 3 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);

    addObject(batch);

    Assertions.assertThat(batch.isEmpty()).as("batch is empty").isFalse();
    Assertions.assertThat(batch.isFull()).as("batch is full").isFalse();

    addObject(batch);

    Assertions.assertThat(batch.isFull()).as("batch is full").isTrue();
  }

  @Test(expected = IllegalStateException.class)
  public void test_add_inFlight() {
    Batch batch = new Batch(10, 10 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);
    batch.prepare();
    addObject(batch);
  }

  @Test
  public void test_prepare() {
    Batch batch = new Batch(3, 3 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);

    addObject(batch);
    addObject(batch);
    addObject(batch);

    Message message = batch.prepare();

    var builder = WeaviateProtoBatch.BatchStreamRequest.newBuilder();
    message.appendTo(builder);
    WeaviateProtoBatch.BatchStreamRequest got = builder.build();

    Assertions.assertThat(got)
        .extracting(WeaviateProtoBatch.BatchStreamRequest::getData).as("data").isNotNull()
        .extracting(WeaviateProtoBatch.BatchStreamRequest.Data::getObjects).as("objects").isNotNull()
        .extracting(WeaviateProtoBatch.BatchStreamRequest.Data.Objects::getValuesCount).as("no. objects")
        .isEqualTo(3);
  }

  @Test(expected = DataTooBigException.class)
  public void test_add_tooBig() {
    int maxSizeBytes = 10 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN;
    Batch batch = new Batch(10, maxSizeBytes);

    // The serialized message is important, raw and ID are not.
    batch.add(new Data(new Object(), "id", bigBatchObject(maxSizeBytes + 10), Data.Type.OBJECT));
  }

  @Test
  public void test_add_overflow() {
    // Batch is just a little short on byte-space for 2 objects,
    // so it should report isFull() after accepting the second one.
    Batch batch = new Batch(3, 2 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN - 1);
    Assertions.assertThat(batch.isEmpty()).as("batch is empty").isTrue();

    addObject(batch);
    addObject(batch);

    Assertions.assertThat(batch.isFull()).as("batch is full after overflow").isTrue();

    batch.clear();
    Assertions.assertThat(batch.isEmpty()).as("batch is not empty after overflow").isFalse();
  }

  @Test
  public void test_clear() {
    Batch batch = new Batch(5, 5 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);
    for (int i = 0; i < 5; i++) {
      addObject(batch);
    }

    Collection<String> removed = batch.clear();
    Assertions.assertThat(removed).as("number of removed items").hasSize(5);
  }

  @Test
  public void test_backlog() {
    Batch batch = new Batch(5, 10 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);

    // Add 11 objects: that's 2 * maxSize + 1
    for (int i = 0; i < 11; i++) {
      addObject(batch);
    }
    Assertions.assertThat(batch.isFull()).as("batch is full").isTrue();

    // The batch is re-populated from the backlog.
    batch.clear();
    Assertions.assertThat(batch.isFull()).as("batch is full").isTrue();

    // The batch is re-populated from the backlog, but it only has 1 item now.
    batch.clear();
    Assertions.assertThat(batch.isEmpty()).as("batch is empty").isFalse();
    Assertions.assertThat(batch.isFull()).as("batch is full").isFalse();
  }

  @Test
  public void test_setMaxSize() {
    // Batch has enough space for 50 objects.
    Batch batch = new Batch(50, 50 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);

    for (int i = 0; i < 20; i++) {
      addObject(batch);
    }
    Assertions.assertThat(batch.isFull())
        .as("batch is full after %d objects", 20)
        .isFalse();

    batch.setMaxSize(40);
    Assertions.assertThat(batch.isFull())
        .as("batch is full after setMaxSize(%d)", 40)
        .isFalse();

    batch.setMaxSize(10);
    Assertions.assertThat(batch.isFull())
        .as("batch is full after setMaxSize(%d)", 10)
        .isTrue();
  }

  @Test
  public void test_setMaxSize_inFlight() {
    // Batch has enough space for a 50 objects.
    Batch batch = new Batch(50, 50 * OBJECT_SIZE_BYTES + MessageSizeUtil.SAFETY_MARGIN);

    for (int i = 0; i < 20; i++) {
      addObject(batch);
    }
    Assertions.assertThat(batch.isFull())
        .as("batch is full after %d objects", 20)
        .isFalse();

    batch.prepare();
    batch.setMaxSize(10);

    // Even though we've technically lowered the limit to 10,
    // it should not be applied until after the batch is cleared.
    Assertions.assertThat(batch.isFull())
        .as("batch is full after in-flight resizing")
        .isFalse();

    batch.clear();

    // After clear, the new limit is applied and the batch
    // should be full after accepting 10 objects.
    for (int i = 0; i < 10; i++) {
      addObject(batch);
    }
    Assertions.assertThat(batch.isFull())
        .as("batch is full after applying pendingMaxSize")
        .isTrue();
  }

  /**
   * Add a WeaviateObject with random UUID to the batch.
   * The size of the serialized object is always {@link OBJECT_SIZE_BYTES}.
   */
  private static void addObject(Batch batch) {
    WeaviateObject<Map<String, Object>> object = WeaviateObject.of();
    batch.add(new Data(object, object.uuid(), newBatchObject(object), Data.Type.OBJECT));
  }

  private static WeaviateProtoBatch.BatchObject newBatchObject(WeaviateObject<Map<String, Object>> object) {
    return InsertManyRequest.buildObject(object, CollectionDescriptor.ofMap("Test"),
        new CollectionHandleDefaults(Optional.of(ConsistencyLevel.ONE), Optional.of("john_doe")));
  }

  private static WeaviateProtoBatch.BatchObject bigBatchObject(int sizeBytes) {
    Random random = new Random();
    Function<Vectors, WeaviateProtoBatch.BatchObject> object = vectors -> newBatchObject(
        WeaviateObject.of(o -> o.vectors(vectors)));

    // Keep adding vectors to the object until we've hit the right size.
    WeaviateProtoBatch.BatchObject out = null;
    Vectors vectors = new Vectors();
    while ((out = object.apply(vectors)).getSerializedSize() < sizeBytes) {
      float[] vector = new float[512];
      for (int i = 0; i < 512; i++) {
        vector[i] = random.nextFloat();
      }
      vectors = vectors.withVectors(Vectors.of(vector));
    }

    assert out.getSerializedSize() >= sizeBytes;
    return out;
  }
}
