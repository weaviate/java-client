package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.grpc.Status;
import io.weaviate.client6.v1.api.collections.batch.Event.Acks;
import io.weaviate.client6.v1.api.collections.batch.Event.Backoff;
import io.weaviate.client6.v1.api.collections.batch.Event.ClientError;
import io.weaviate.client6.v1.api.collections.batch.Event.Oom;
import io.weaviate.client6.v1.api.collections.batch.Event.Results;
import io.weaviate.client6.v1.api.collections.batch.Event.Started;
import io.weaviate.client6.v1.api.collections.batch.Event.StreamHangup;
import io.weaviate.client6.v1.api.collections.batch.Event.TerminationEvent;

sealed interface Event
    permits Started, Acks, Results, Backoff, Oom, TerminationEvent, StreamHangup, ClientError {

  final static Event STARTED = new Started();
  final static Event SHUTTING_DOWN = TerminationEvent.SHUTTING_DOWN;
  final static Event EOF = TerminationEvent.EOF;

  /**
   * The server has acknowledged our Start message and is ready to receive data.
   */
  record Started() implements Event {
  }

  /**
   * The server has added items from the previous message to its internal
   * work queue, client MAY send the next batch.
   *
   * <p>
   * The protocol guarantess that {@link Acks} will contain IDs for all
   * items sent in the previous batch.
   */
  record Acks(Collection<String> acked) implements Event {
    public Acks {
      acked = List.copyOf(requireNonNull(acked, "acked is null"));
    }

    @Override
    public String toString() {
      return "Acks";
    }
  }

  /**
   * Results for the insertion of a previous batches.
   *
   * <p>
   * We assume that the server may return partial results, or return
   * results out of the order of inserting messages.
   */
  record Results(Collection<String> successful, Map<String, String> errors) implements Event {
    public Results {
      successful = List.copyOf(requireNonNull(successful, "successful is null"));
      errors = Map.copyOf(requireNonNull(errors, "errors is null"));
    }

    @Override
    public String toString() {
      return "Results";
    }
  }

  /**
   * Backoff communicates the optimal batch size (number of objects)
   * with respect to the current load on the server.
   *
   * <p>
   * Backoff is an instruction, not a recommendation.
   * On receiving this message, the client must ensure that
   * all messages it produces, including the one being prepared,
   * do not exceed the size limit indicated by {@link #maxSize}
   * until the server sends another Backoff message. The limit
   * MUST also be respected after a {@link BatchContext#reconnect}.
   *
   * <p>
   * The client MAY use the latest {@link #maxSize} as the default
   * message limit in a new {@link BatchContext}, but is not required to.
   */
  record Backoff(int maxSize) implements Event {

    @Override
    public String toString() {
      return "Backoff";
    }
  }

  /**
   * <strong>Out-Of-Memory</strong>.
   *
   * <p>
   * Items sent in the previous request cannot be accepted,
   * as inserting them may exhaust server's available disk space.
   * On receiving this message, the client MUST stop producing
   * messages immediately and await {@link #SHUTTING_DOWN} event.
   *
   * <p>
   * Oom is the sibling of {@link Acks} with the opposite effect.
   * The protocol guarantees that the server will respond with either of
   * the two, but never both.
   */
  record Oom(int delaySeconds) implements Event {
  }

  /** Events that are part of the server's graceful shutdown strategy. */
  enum TerminationEvent implements Event {
    /**
     * <strong>Server shutdown in progress.</stong>
     *
     * <p>
     * The server began the process of gracefull shutdown, due to a
     * scale-up event (if it previously reported {@link #OOM}) or
     * some other external event.
     * On receiving this message, the client MUST stop producing
     * messages immediately, close it's side of the stream, and
     * continue readings server's messages until {@link #EOF}.
     */
    SHUTTING_DOWN,

    /**
     * <strong>Stream EOF.</stong>
     *
     * <p>
     * The server has will not receive any messages. If the client
     * has more data to send, it SHOULD re-connect to another instance
     * by re-opening the stream and continue processing the batch.
     * If the client has previously sent {@link Message#STOP}, it can
     * safely exit.
     */
    EOF;
  }

  /**
   * StreamHangup means the RPC is "dead": the stream is closed
   * and using it will result in an {@link IllegalStateException}.
   */
  record StreamHangup(Exception exception) implements Event {
    static StreamHangup fromThrowable(Throwable t) {
      Status status = Status.fromThrowable(t);
      return new StreamHangup(status.asException());
    }
  }

  /**
   * ClientError means a client-side exception has happened,
   * and is meant primarily for the "send" thread to propagate
   * any exception it might catch.
   *
   * <p>
   * This MUST be treated as an irrecoverable condition, because
   * it is likely caused by an internal issue (an NPE) or a bad
   * input ({@link DataTooBigException}).
   */
  record ClientError(Exception exception) implements Event {
  }
}
