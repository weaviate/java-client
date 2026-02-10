package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import io.weaviate.client6.v1.api.collections.batch.Event.Backoff;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;

// assert maxSize > 0 : "non-positive maxSize";
// assert maxSizeBytes > 0 : "non-positive maxSizeBytes";
// assert sizeBytes >= 0 : "negative sizeBytes";
// assert buffer.size() <= maxSize : "buffer exceeds maxSize";
// assert sizeBytes <= maxSizeBytes : "message exceeds maxSizeBytes";
// if (buffer.size() < maxSize) {
//   assert backlog.isEmpty() : "backlog not empty when buffer not full";
// }
// if (buffer.isEmpty()) {
//   assert sizeBytes == 0 : "sizeBytes must be 0 when buffer is empty";
// }
// assert pendingMaxSize != null : "pending max size is null";

/**
 * Batch can be in either of 2 states:
 * <ul>
 * <li><strong>Open</strong> batch accepts new items and can be resized.
 * <li><strong>In-flight</strong> batch is sealed: it rejects new items and
 * avoids otherwise modifying the {@link #buffer} until it's cleared.
 * </ul>
 *
 * <h2>Class invariants</h2>
 *
 * {@link #maxSize} and {@link #maxSizeBytes} MUST be positive.
 * A batch with {@code cap=0} is not useful. <br>
 * {@link #buffer} size and {@link #sizeBytes} MUST be non-negative. <br>
 * {@link #buffer} size MUST NOT exceed {@link #maxSize}. <br>
 * {@link #sizeBytes} MUST NOT exceed {@link #maxSize}. <br>
 * {@link #sizeBytes} MUST be 0 if the buffer is full. <br>
 * {@link #backlog} MAY only contain items when {@link #buffer} is full. In the
 * {@link #pendingMaxSize} is empty for an open batch. <br>
 * edge-case
 *
 *
 * <h2>Synchronization policy</h2>
 *
 * @see #inFlight
 * @see #isFull
 * @see #clear
 * @see #checkInvariants
 */
@ThreadSafe
final class Batch {
  /** Backlog MUST be confined to the "receiver" thread. */
  private final List<Data> backlog = new ArrayList<>();

  /**
   * Items stored in this batch.
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

  /** Total serialized size of the items in the {@link #buffer}. */
  @GuardedBy("this")
  private long sizeBytes;

  /** An in-flight batch is unmodifiable. */
  @GuardedBy("this")
  private boolean inFlight = false;

  /**
   * Pending update to the {@link #maxSize}.
   *
   * The value is non-empty when {@link #setMaxSize} is called
   * while the batch is {@link #inFlight}.
   */
  @GuardedBy("this")
  private OptionalInt pendingMaxSize = OptionalInt.empty();

  Batch(int maxSize, int maxSizeBytes) {
    assert maxSize > 0 : "non-positive maxSize";

    this.maxSizeBytes = MessageSizeUtil.maxSizeBytes(maxSizeBytes);
    this.maxSize = maxSize;
    this.buffer = new LinkedHashMap<>(maxSize); // LinkedHashMap preserves insertion order.

    checkInvariants();
  }

  /**
   * Returns true if batch has reached its capacity, either in terms
   * of the item count or the batch's estimated size in bytes.
   */
  synchronized boolean isFull() {
    return buffer.size() == maxSize || sizeBytes == maxSizeBytes;
  }

  /**
   * Returns true if the batch's internal buffer is empty.
   * If it's primary buffer is empty, its backlog is guaranteed
   * to be empty as well.
   */
  synchronized boolean isEmpty() {
    return buffer.isEmpty(); // sizeBytes == 0 is guaranteed by class invariant.
  }

  /**
   * Prepare a request to be sent. After calling this method, this batch becomes
   * "in-flight": an attempt to {@link #add} more items to it will be rejected
   * with an exception.
   */
  synchronized Message prepare() {
    checkInvariants();

    inFlight = true;
    return builder -> {
      buffer.forEach((__, data) -> {
        data.appendTo(builder);
      });
    };
  }

  /**
   * Set the new {@link #maxSize} for this buffer.
   *
   * <p>
   * How the size is applied depends of the buffer's current state:
   * <ul>
   * <li>When the batch is in-flight, the new limit is stored in
   * {@link #pendingMaxSize} and will be applied once the batch is cleared.
   * <li>While the batch is still open, the new limit is applied immediately and
   * the {@link #pendingMaxSize} is set back to {@link OptionalInt#empty}. If
   * the current buffer size exceeds the new limit, the overflow items are moved
   * to the {@link #backlog}.
   * </ul>
   *
   * @param maxSizeNew New batch size limit.
   *
   * @see #clear
   */
  synchronized void setMaxSize(int maxSizeNew) {
    checkInvariants();

    try {
      // In-flight batch cannot be modified.
      // Store the requested maxSize for later;
      // it will be applied on the next ack.
      if (inFlight) {
        pendingMaxSize = OptionalInt.of(maxSizeNew);
        return;
      }

      maxSize = maxSizeNew;
      pendingMaxSize = OptionalInt.empty();

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
   * Add a data item to the batch.
   *
   *
   * @throws DataTooBigException   If the data exceeds the maximum
   *                               possible batch size.
   * @throws IllegalStateException If called on an "in-flight" batch.
   * @see #prepare
   * @see #inFlight
   *
   * @return Boolean indicating if the item has been accepted.
   */
  synchronized boolean add(Data data) throws IllegalStateException, DataTooBigException {
    requireNonNull(data, "data is null");
    checkInvariants();

    try {
      if (inFlight) {
        throw new IllegalStateException("Batch is in-flight");
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

  /**
   * Add a data item to the batch.
   *
   * This method does not check {@link Data#sizeBytes}, so the caller
   * must ensure that this item will not overflow the batch.
   */
  private synchronized void addSafe(Data data) {
    buffer.put(data.id(), data);
    sizeBytes += data.sizeBytes();
  }

  /**
   * Clear this batch's internal buffer.
   *
   * <p>
   * Once the buffer is pruned, it is re-populated from the backlog
   * until the former is full or the latter is exhaused.
   * If {@link #pendingMaxSize} is not empty, it is applied
   * before re-populating the buffer.
   *
   * @return IDs removed from the buffer.
   */
  synchronized Collection<String> clear() {
    checkInvariants();

    try {
      inFlight = false;

      Set<String> removed = Set.copyOf(buffer.keySet());
      buffer.clear();

      if (pendingMaxSize.isPresent()) {
        setMaxSize(pendingMaxSize.getAsInt());
      }

      // Populate internal buffer from the backlog.
      // We don't need to check the return value of .add(),
      // as all items in the backlog are guaranteed to not
      // exceed maxSizeBytes.
      backlog.stream()
          .takeWhile(__ -> !isFull())
          .forEach(this::addSafe);

      return removed;
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
    if (!isFull()) {
      assert backlog.isEmpty() : "backlog not empty when buffer not full";
    }
    if (buffer.isEmpty()) {
      assert sizeBytes == 0 : "sizeBytes must be 0 when buffer is empty";
    }

    requireNonNull(pendingMaxSize, "pendingMaxSize is null");
    if (!inFlight) {
      assert pendingMaxSize.isEmpty() : "open batch has pending maxSize";
    }
  }
}
