package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;

import com.google.protobuf.CodedOutputStream;
import com.nimbusds.jose.shaded.jcip.ThreadSafe;

import io.weaviate.client6.v1.api.collections.batch.Event.Backoff;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

/**
 * Message, along with its items, can be in either of 2 states:
 * <ul>
 * <li>Prepared message accepts new items and can be resized.
 * <li>In-flight message if sealed: it rejects new items and avoids otherwise
 * modifying the {@link #buffer} until it's ack'ed.
 * </ul>
 *
 * <h2>Class invariants</h2>
 * Buffer size cannot exceed {@link #maxSize}.
 *
 * <h2>Synchronization policy</h2>
 */
@ThreadSafe
final class Message {
  private static int DATA_TAG_SIZE = CodedOutputStream
      .computeTagSize(WeaviateProtoBatch.BatchStreamRequest.DATA_FIELD_NUMBER);

  /** Backlog MUST be confined to the "receiver" thread. */
  private final List<Data> backlog = new ArrayList<>();

  /**
   * Items stored in this message.
   */
  @GuardedBy("this")
  private final LinkedHashMap<String, Data> buffer;

  /**
   * Maximum number of items that can be added to the request.
   * Must be greater that zero.
   *
   * <p>
   * This is determined by the server's {@link Backoff} instruction.
   */
  @GuardedBy("this")
  private int maxSize;

  /**
   * Maximum size of the serialized message in bytes.
   * Must be greater that zero.
   *
   * <p>
   * This is determined by the {@link GrpcChannelOptions#maxMessageSize}.
   */
  @GuardedBy("this")
  private final long maxSizeBytes;

  /** Total size of all values in the buffer. */
  @GuardedBy("this")
  private long sizeBytes;

  @GuardedBy("this")
  private boolean inFlight = false;

  @GuardedBy("this")
  private OptionalInt pendingMaxSize = OptionalInt.empty();

  Message(int maxSize, int maxSizeBytes) {
    assert maxSize > 0 : "non-positive maxSize";

    // A protobuf field has layout {@code [tag][lenght(payload)][payload]},
    // so to estimate the message size correctly we must account for "tag"
    // and "length", not just the raw payload.
    if (maxSizeBytes <= DATA_TAG_SIZE) {
      throw new IllegalArgumentException("Maximum message size must be at least %dB".formatted(DATA_TAG_SIZE));
    }
    this.maxSizeBytes = maxSizeBytes - DATA_TAG_SIZE;
    this.maxSize = maxSize;
    this.buffer = new LinkedHashMap<>(maxSize); // LinkedHashMap preserves insertion order.

    checkInvariants();
  }

  /**
   * Returns true if message has reached its capacity, either in terms
   * of the item count or the message's estimated size in bytes.
   */
  synchronized boolean isFull() {
    return buffer.size() == maxSize || sizeBytes == maxSizeBytes;
  }

  /**
   * Returns true if the message's internal buffer is empty.
   * If it's primary buffer is empty, its backlog is guaranteed
   * to be empty as well.
   */
  synchronized boolean isEmpty() {
    return buffer.isEmpty(); // sizeBytes == 0 is guaranteed by class invariant.
  }

  /**
   * Prepare a request to be sent. After calling this method, this message becomes
   * "in-flight": an attempt to {@link #add} more items to it will be rejected
   * with an exception.
   */
  synchronized StreamMessage prepare() {
    inFlight = true;
    return builder -> {
      buffer.forEach((__, data) -> {
        data.appendTo(builder);
      });
    };
  }

  synchronized void setMaxSize(int maxSizeNew) {
    try {
      // In-flight message cannot be modified.
      // Store the requested maxSize for later;
      // it will be applied on the next ack.
      if (inFlight) {
        pendingMaxSize = OptionalInt.of(maxSizeNew);
        return;
      }

      maxSize = maxSizeNew;

      // Buffer still fits under the new limit.
      if (buffer.size() <= maxSize) {
        return;
      }

      // Buffer exceeds the new limit.
      // Move extra items to the backlog in LIFO order.
      Iterator<Map.Entry<String, Data>> extra = buffer.reversed()
          .entrySet().stream()
          .limit(buffer.size() - maxSize)
          .iterator();

      while (extra.hasNext()) {
        Map.Entry<String, Data> next = extra.next();
        backlog.add(next.getValue());
        extra.remove();
      }
      // Reverse the backlog to restore the FIFO order.
      Collections.reverse(backlog);
    } finally {
      checkInvariants();
    }
  }

  /**
   * Add a data item to the message.
   *
   *
   * @throws DataTooBigException   If the data exceeds the maximum
   *                               possible message size.
   * @throws IllegalStateException If called on an "in-flight" message.
   * @see #prepare
   * @see #inFlight
   *
   * @return Boolean indicating if the item has been accepted.
   */
  synchronized boolean add(Data data) throws IllegalStateException, DataTooBigException {
    requireNonNull(data, "data is null");

    try {
      if (inFlight) {
        throw new IllegalStateException("Message is in-flight");
      }
      if (data.sizeBytes() > maxSizeBytes - sizeBytes) {
        if (isEmpty()) {
          throw new DataTooBigException(data, maxSizeBytes);
        }
        return false;
      }
      addSafe(data);
      return true;
    } finally {
      checkInvariants();
    }
  }

  private synchronized void addSafe(Data data) {
    buffer.put(data.id(), data);
    sizeBytes += data.sizeBytes();
  }

  synchronized Iterable<String> ack(Iterable<String> acked) {
    requireNonNull(acked, "acked are null");

    try {
      acked.forEach(id -> buffer.remove(id));
      Set<String> remaining = Set.copyOf(buffer.keySet());

      // Reset the in-flight status.
      inFlight = false;

      // Populate message from the backlog.
      // We don't need to check the return value of .add(),
      // as all items in the backlog are guaranteed to not
      // exceed maxSizeBytes.
      backlog.stream()
          .takeWhile(__ -> !isFull())
          .forEach(this::addSafe);

      return remaining;
    } finally {
      checkInvariants();
    }
  }

  /** Asserts the invariants of this class. */
  private synchronized void checkInvariants() {
    assert maxSize > 0 : "non-positive maxSize";
    assert maxSizeBytes > 0 : "non-positive maxSizeBytes";
    assert sizeBytes >= 0 : "negative sizeBytes";
    assert buffer.size() <= maxSize : "buffer exceeds maxSize";
    assert sizeBytes <= maxSizeBytes : "message exceeds maxSizeBytes";
    if (buffer.size() < maxSize) {
      assert backlog.isEmpty() : "backlog not empty when buffer not full";
    }
    if (buffer.isEmpty()) {
      assert sizeBytes == 0 : "sizeBytes must be 0 when buffer is empty";
    }
    assert pendingMaxSize != null : "pending max size is null";
  }
}
