package io.weaviate.client6.v1.api.collections.batch;

import io.weaviate.client6.v1.api.WeaviateException;

/**
 * DuplicateTaskException is thrown if task is submitted to the batch
 * while another task with the same ID is in progress.
 */
public class DuplicateTaskException extends WeaviateException {
  private final TaskHandle existing;

  DuplicateTaskException(TaskHandle duplicate, TaskHandle existing) {
    super("%s cannot be added to the batch while another task with the same ID is in progress");
    this.existing = existing;
  }

  /**
   * Get the currently in-progress handle that's a duplicate of the one submitted.
   */
  public TaskHandle getExisting() {
    return existing;
  }
}
