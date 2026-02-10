package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.concurrent.ThreadSafe;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.ObjectReference;

@ThreadSafe
@SuppressWarnings("deprecation") // protoc uses GeneratedMessageV3
public final class TaskHandle {
  static final TaskHandle POISON = new TaskHandle();

  /**
   * Input value as passed by the user.
   *
   * <p>
   * Changes in the {@link #raw}'s underlying value will not be reflected
   * in the {@link TaskHandle} (e.g. the serialized version is not updated),
   * so users SHOULD treat items passed to and retrieved from {@link TaskHandle}
   * as effectively ummodifiable.
   */
  private final Data data;

  /** Flag indicatig the task has been ack'ed. */
  private final CompletableFuture<Void> acked = new CompletableFuture<>();

  public final record Result(Optional<String> error) {
    public Result {
      requireNonNull(error, "error is null");
    }
  }

  /**
   * Task result completes when the client receives {@link Event.Results}
   * containing this handle's {@link #id}.
   */
  private final CompletableFuture<Result> result = new CompletableFuture<>();

  /** The number of times this task has been retried. */
  private final int retries;

  private TaskHandle(Data data, int retries) {
    this.data = requireNonNull(data, "data is null");

    assert retries >= 0 : "negative retries";
    this.retries = retries;
  }

  /** Constructor for {@link WeaviateObject}. */
  TaskHandle(WeaviateObject<?> object, GeneratedMessage.ExtendableMessage<GeneratedMessageV3> data) {
    this(new Data(object, object.uuid(), data, Data.Type.OBJECT), 0);
  }

  /** Constructor for {@link ObjectReference}. */
  TaskHandle(ObjectReference reference, GeneratedMessage.ExtendableMessage<GeneratedMessageV3> data) {
    this(new Data(reference, reference.beacon(), data, Data.Type.REFERENCE), 0);
  }

  /**
   * Poison pill constructor.
   *
   * <p>
   * A handle created with this constructor should not be
   * used for anything other that direct comparison using {@code ==} operator;
   * calling any method on a poison pill is likely to result in a
   * {@link NullPointerException} being thrown.
   */
  private TaskHandle() {
    this.data = null;
    this.retries = 0;
  }

  /**
   * Creates a new task containing the same data as this task and {@link retries}
   * counter incremented by 1. The {@link acked} and {@link result} futures
   * are not copied to the returned task.
   *
   * @return Task handle.
   */
  TaskHandle retry() {
    return new TaskHandle(data, retries + 1);
  }

  String id() {
    return data.id();
  }

  /** Set the {@link #acked} flag. */
  void setAcked() {
    acked.complete(null);
  }

  /**
   * Mark the task successful. This status cannot be changed, so calling
   * {@link #setError} afterwards will have no effect.
   */
  void setSuccess() {
    setResult(new Result(Optional.empty()));
  }

  /**
   * Mark the task failed. This status cannot be changed, so calling
   * {@link #setSuccess} afterwards will have no effect.
   *
   * @param error Error message. Null values are tolerated, but are only expected
   *              to occurr due to a server's mistake.
   *              Do not use {@code setError(null)} if the server reports success
   *              status for the task; prefer {@link #setSuccess} in that case.
   */
  void setError(String error) {
    setResult(new Result(Optional.ofNullable(error)));
  }

  /**
   * Set result for this task.
   *
   * @throws IllegalStateException if the task has not been ack'ed.
   */
  private void setResult(Result result) {
    if (!acked.isDone()) {
      throw new IllegalStateException("Result can only be set for an ack'ed task");
    }
    this.result.complete(result);
  }

  /**
   * Check if the task has been accepted.
   *
   * @return A future which completes when the server has accepted the task.
   */
  public CompletableFuture<Void> isAcked() {
    return acked;
  }

  /**
   * Retrieve the result for this task.
   *
   * @return A future which completes when the server
   *         has reported the result for this task.
   */
  public CompletableFuture<Result> result() {
    return result;
  }

  /**
   * Number of times this task has been retried. Since {@link TaskHandle} is
   * immutable, this value does not change, but retrying a task via
   * {@link BatchContext#retry} is reflected in the returned handle's
   * {@link #timesRetried}.
   */
  public int timesRetried() {
    return retries;
  }
}
