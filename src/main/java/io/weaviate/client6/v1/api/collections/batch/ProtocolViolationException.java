package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.List;

import io.weaviate.client6.v1.api.WeaviateException;

/**
 * ProtocolViolationException describes unexpected server behavior in violation
 * of the SSB protocol.
 *
 * <p>
 * This exception cannot be handled in a meaningful way and should be reported
 * to the upstream <a href="https://github.com/weaviate/weaviate">Weaviate</a>
 * project.
 */
public class ProtocolViolationException extends WeaviateException {
  ProtocolViolationException(String message) {
    super(message);
  }

  /**
   * Protocol violated because an event arrived while the client is in a state
   * which doesn't expect to handle this event.
   *
   * @param current Current {@link BatchContext} state.
   * @param event   Server-side event.
   * @return ProtocolViolationException with a formatted message.
   */
  static ProtocolViolationException illegalStateTransition(State current, Event event) {
    return new ProtocolViolationException("%s arrived in %s state".formatted(event, current));
  }

  /**
   * Protocol violated because some tasks from the previous Data message
   * are not present in the Acks message.
   *
   * @param remaining IDs of the tasks that weren't ack'ed. MUST be non-empty.
   * @return ProtocolViolationException with a formatted message.
   */
  static ProtocolViolationException incompleteAcks(List<String> remaining) {
    requireNonNull(remaining, "remaining is null");
    return new ProtocolViolationException("IDs from previous Data message missing in Acks: '%s', ... (%d more)"
        .formatted(remaining.get(0), remaining.size() - 1));

  }
}
