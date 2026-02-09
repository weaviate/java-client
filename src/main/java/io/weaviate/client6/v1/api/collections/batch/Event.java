package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.weaviate.client6.v1.api.collections.batch.Event.Acks;
import io.weaviate.client6.v1.api.collections.batch.Event.Backoff;
import io.weaviate.client6.v1.api.collections.batch.Event.Results;
import io.weaviate.client6.v1.api.collections.batch.Event.Started;
import io.weaviate.client6.v1.api.collections.batch.Event.TerminationEvent;

sealed interface Event
    permits Started, Acks, Results, Backoff, TerminationEvent {

  final static Event STARTED = new Started();
  final static Event OOM = TerminationEvent.OOM;
  final static Event SHUTTING_DOWN = TerminationEvent.SHUTTING_DOWN;
  final static Event SHUTDOWN = TerminationEvent.SHUTDOWN;

  /** */
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
  }

  enum TerminationEvent implements Event {
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
     * {@link #OOM} is the sibling of {@link Acks} with the opposite effect.
     * The protocol guarantees that the server will respond with either of
     * the two, but never both.
     */
    OOM,

    /**
     * <strong>Server shutdown in progress.</stong>
     *
     * <p>
     * The server began the process of gracefull shutdown, due to a
     * scale-up event (if it previously reported {@link #OOM}) or
     * some other external event.
     * On receiving this message, the client MUST stop producing
     * messages immediately and close it's side of the stream.
     */
    SHUTTING_DOWN,

    /**
     * <strong>Server is shutdown.</stong>
     *
     * <p>
     * The server has finished the shutdown process and will not
     * receive any messages. On receiving this message, the client
     * MUST continue reading messages in the stream until the server
     * closes it on its end, then re-connect to another instance
     * by re-opening the stream and continue processing the batch.
     */
    SHUTDOWN;
  }

}
