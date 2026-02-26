package io.weaviate.client6.v1.api.collections.batch;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.InsertManyRequest;
import io.weaviate.client6.v1.api.collections.data.ObjectReference;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class TaskHandleTest {
  private static final WeaviateObject<Map<String, Object>> OBJECT = WeaviateObject.of();
  private static final BatchReference REFERENCE = new BatchReference(
      "Songs", "hasAwards", "song-1",
      ObjectReference.collection("GrammyAwards", "grammy-1"));

  private static final GeneratedMessage.ExtendableMessage<GeneratedMessageV3> REFERENCE_PROTO = InsertManyRequest
      .buildReference(REFERENCE, Optional.empty());
  private static final GeneratedMessage.ExtendableMessage<GeneratedMessageV3> OBJECT_PROTO = InsertManyRequest
      .buildObject(OBJECT,
          CollectionDescriptor.ofMap("Songs"),
          CollectionHandleDefaults.of(CollectionHandleDefaults.none()));

  @Test
  public void test_newTaskHandle_WeaviateObject_success() {
    TaskHandle taskHandle = new TaskHandle(OBJECT, OBJECT_PROTO);

    Assertions.assertThat(taskHandle)
        .returns(OBJECT.uuid(), TaskHandle::id)
        .returns(0, TaskHandle::timesRetried);

    assertAcked(taskHandle, false);
    assertHasResult(taskHandle, false);

    taskHandle.setAcked();

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, false);

    taskHandle.setSuccess();

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, true)
        .extracting(future -> future.getNow(null)).isNotNull()
        .extracting(TaskHandle.Result::error, InstanceOfAssertFactories.optional(String.class))
        .isEmpty();
  }

  @Test
  public void test_newTaskHandle_WeaviateObject_error() {
    TaskHandle taskHandle = new TaskHandle(OBJECT, OBJECT_PROTO);

    Assertions.assertThat(taskHandle)
        .returns(OBJECT.uuid(), TaskHandle::id)
        .returns(0, TaskHandle::timesRetried);

    assertAcked(taskHandle, false);
    assertHasResult(taskHandle, false);

    taskHandle.setAcked();

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, false);

    taskHandle.setError("Whaam!");

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, true)
        .extracting(future -> future.getNow(null)).isNotNull()
        .extracting(TaskHandle.Result::error, InstanceOfAssertFactories.optional(String.class))
        .get().isEqualTo("Whaam!");
  }

  @Test
  public void test_newTaskHandle_BatchReference_success() {
    TaskHandle taskHandle = new TaskHandle(REFERENCE, REFERENCE_PROTO);

    Assertions.assertThat(taskHandle)
        .returns(REFERENCE.target().beacon(), TaskHandle::id)
        .returns(0, TaskHandle::timesRetried);

    assertAcked(taskHandle, false);
    assertHasResult(taskHandle, false);

    taskHandle.setAcked();

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, false);

    taskHandle.setSuccess();

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, true)
        .extracting(future -> future.getNow(null)).isNotNull()
        .extracting(TaskHandle.Result::error, InstanceOfAssertFactories.optional(String.class))
        .isEmpty();
  }

  @Test
  public void test_newTaskHandle_BatchReference_error() {
    TaskHandle taskHandle = new TaskHandle(REFERENCE, REFERENCE_PROTO);

    Assertions.assertThat(taskHandle)
        .returns(REFERENCE.target().beacon(), TaskHandle::id)
        .returns(0, TaskHandle::timesRetried);

    assertAcked(taskHandle, false);
    assertHasResult(taskHandle, false);

    taskHandle.setAcked();

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, false);

    taskHandle.setError("Whaam!");

    assertAcked(taskHandle, true);
    assertHasResult(taskHandle, true)
        .extracting(future -> future.getNow(null)).isNotNull()
        .extracting(TaskHandle.Result::error, InstanceOfAssertFactories.optional(String.class))
        .get().isEqualTo("Whaam!");
  }

  @Test
  public void test_retry() {
    TaskHandle taskHandle = new TaskHandle(OBJECT, OBJECT_PROTO);
    Assertions.assertThat(taskHandle).returns(0, TaskHandle::timesRetried);

    TaskHandle retried;
    Assertions.assertThat(retried = taskHandle.retry())
        .returns(1, TaskHandle::timesRetried)
        .extracting(TaskHandle::data).isEqualTo(taskHandle.data());

    Assertions.assertThat(retried = retried.retry())
        .returns(2, TaskHandle::timesRetried)
        .extracting(TaskHandle::data).isEqualTo(taskHandle.data());
  }

  @Test
  public void test_toString_POISON() {
    Assertions.assertThat(TaskHandle.POISON.toString())
        .isEqualTo("TaskHandle<POISON>");
  }

  private void assertAcked(TaskHandle taskHandle, boolean expect) {
    Assertions.assertThat(taskHandle)
        .extracting(TaskHandle::isAcked)
        .as("expect is acked")
        .returns(expect, CompletableFuture::isDone);
  }

  private AbstractObjectAssert<?, CompletableFuture<TaskHandle.Result>> assertHasResult(TaskHandle taskHandle,
      boolean expect) {
    return Assertions.assertThat(taskHandle)
        .extracting(TaskHandle::result)
        .as("expect has result")
        .returns(expect, CompletableFuture::isDone);
  }
}
