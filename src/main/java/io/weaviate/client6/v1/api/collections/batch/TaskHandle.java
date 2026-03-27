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
  /**
   * Poison pill informs the "sender" that the batch is closed
   * and no more user-supplied items will be incoming.
   */
  static final TaskHandle POISON = new TaskHandle("POISON");
  /**
   * Break pill informs the "sender" that the event handler has finished
   * processing an incoming {@link Event.Results} message and no more retry items
   * will be added to the queue until the next message arrives.
   */
  static final TaskHandle END_RESULTS = new TaskHandle("END_RESULTS");

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
   * Poison / Break pill constructor.
   *
   * <p>
   * A handle created with this constructor should not be
   * used for anything other that direct comparison using {@code ==} operator;
   * calling any method on a poison pill is likely to result in a
   * {@link NullPointerException} being thrown.
   */
  private TaskHandle(String name) {
    super(name, RetryPolicy.never(), __ -> {
    });
    this.data = null;
  }

  Data data() {
    return data;
  }

  @Override
  public String toString() {
    if (this == POISON || this == END_RESULTS) {
      return "TaskHandle<%s>".formatted(id());
    }
    return "TaskHandle<id=%s, retried=%d, created=%s>".formatted(id(), timesRetried(), createdAt);
  }
}
