package io.weaviate.client6.v1.api.collections.batch;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
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
  private static final RetryPolicy RETRY_POLICY = RetryPolicy.never();

  @Test
  public void test_success() {
    RetriableTask task = new RetriableTask("ok", RetryPolicy.never(), AssertionError::new) {
    };
    Assertions.assertThat(task.done()).isNotCompleted();

    task.setSuccess();
    Assertions.assertThat(task.done()).isCompleted();
  }

  @Test
  public void test_error() {
    RetriableTask task = new RetriableTask("failed", RetryPolicy.never(), AssertionError::new) {
    };
    Assertions.assertThat(task.done()).isNotCompleted();

    task.setError(new ServerException("Whaam!"));
    Assertions.assertThat(task.done())
        .isCompletedExceptionally()
        .withFailMessage("Whaam!");
  }

  @Test
  public void test_retryAndSucceed() {
    RetryPolicy retryOnce = new RetryPolicy(1);
    RetriableTask task = new RetriableTask(
        "retry_me",
        retryOnce,
        id -> Assertions.assertThat(id).isEqualTo("retry_me")) {
    };
    Assertions.assertThat(task.done()).isNotCompleted();

    task.setError(new ServerException("Whaam!"));
    Assertions.assertThat(task.done()).isNotCompleted();
    Assertions.assertThat(task.done()).isNotCompletedExceptionally();
    Assertions.assertThat(task.timesRetried()).isEqualTo(1);

    task.setSuccess();
    Assertions.assertThat(task.done()).isCompleted();
  }

  @Test
  public void test_retryAndFail() {
    RetryPolicy retryOnce = new RetryPolicy(1);
    RetriableTask task = new RetriableTask(
        "retry_me",
        retryOnce,
        id -> Assertions.assertThat(id).isEqualTo("retry_me")) {
    };
    Assertions.assertThat(task.done()).isNotCompleted();

    task.setError(new ServerException("Whaam-1!"));
    Assertions.assertThat(task.done()).isNotCompleted();
    Assertions.assertThat(task.done()).isNotCompletedExceptionally();
    Assertions.assertThat(task.timesRetried()).isEqualTo(1);

    task.setError(new ServerException("Whaam-2!"));
    Assertions.assertThat(task.timesRetried()).isEqualTo(1);
    Assertions.assertThat(task.done())
        .isCompletedExceptionally()
        .withFailMessage("Whaam-2!");
  }

  @Test
  public void test_newTaskHandle_WeaviateObject() {
    TaskHandle taskHandle = new TaskHandle(OBJECT, OBJECT_PROTO, RETRY_POLICY, AssertionError::new);
    Assertions.assertThat(taskHandle).returns(OBJECT.uuid(), TaskHandle::id);
  }

  @Test
  public void test_newTaskHandle_BatchReference() {
    TaskHandle taskHandle = new TaskHandle(REFERENCE, REFERENCE_PROTO, RETRY_POLICY, AssertionError::new);
    Assertions.assertThat(taskHandle).returns(REFERENCE.target().beacon(), TaskHandle::id);
  }

  @Test
  public void test_toString_POISON() {
    Assertions.assertThat(TaskHandle.POISON.toString())
        .isEqualTo("TaskHandle<POISON>");
  }

  private AbstractObjectAssert<?, CompletableFuture<Void>> assertHasResult(TaskHandle taskHandle,
      boolean expect) {
    return Assertions.assertThat(taskHandle)
        .extracting(TaskHandle::done)
        .as("expect has result")
        .returns(expect, CompletableFuture::isDone);
  }
}
