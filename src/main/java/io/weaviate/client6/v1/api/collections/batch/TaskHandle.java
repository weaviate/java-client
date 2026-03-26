package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.function.Consumer;

import javax.annotation.concurrent.ThreadSafe;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.BatchReference;

@ThreadSafe
@SuppressWarnings("deprecation") // protoc uses GeneratedMessageV3
public final class TaskHandle extends RetriableTask {
  static final TaskHandle POISON = new TaskHandle();

  /**
   * Input value as passed by the user.
   *
   * <p>
   * Changes in the {@link Data}'s underlying value will not be reflected
   * in the {@link TaskHandle} (e.g. the serialized version is not updated),
   * so users SHOULD treat items passed to and retrieved from {@link TaskHandle}
   * as effectively unmodifiable.
   */
  private final Data data;

  /** Task creation timestamp. */
  private final Instant createdAt = Instant.now();

  /** Constructor for {@link WeaviateObject}. */
  TaskHandle(
      WeaviateObject<?> object,
      GeneratedMessage.ExtendableMessage<GeneratedMessageV3> data,
      RetryPolicy retryPolicy, Consumer<String> onRetry) {
    this(new Data(object, object.uuid(), data, Data.Type.OBJECT),
        retryPolicy, onRetry);
  }

  /** Constructor for {@link BatchReference}. */
  TaskHandle(
      BatchReference reference,
      GeneratedMessage.ExtendableMessage<GeneratedMessageV3> data,
      RetryPolicy retryPolicy, Consumer<String> onRetry) {
    this(new Data(reference, reference.target().beacon(), data, Data.Type.REFERENCE),
        retryPolicy, onRetry);
  }

  private TaskHandle(Data data, RetryPolicy retryPolicy, Consumer<String> onRetry) {
    super(requireNonNull(data, "data is null").id(), retryPolicy, onRetry);
    this.data = requireNonNull(data, "data is null");
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
    super("POISON", RetryPolicy.never(), __ -> {
    });
    this.data = null;
  }

  Data data() {
    return data;
  }

  @Override
  public String toString() {
    if (this == POISON) {
      return "TaskHandle<POISON>";
    }
    return "TaskHandle<id=%s, retried=%d, created=%s>".formatted(id(), timesRetried(), createdAt);
  }
}
