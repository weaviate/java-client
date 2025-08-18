package io.weaviate.client6.v1.api.collections;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class CollectionHandleDefaultsTest {
  private static final CollectionDescriptor<Map<String, Object>> DESCRIPTOR = CollectionDescriptor.ofMap("Things");

  /** CollectionHandle with no defaults. */
  private static final CollectionHandle<Map<String, Object>> HANDLE_NONE = new CollectionHandle<>(
      null, null,
      DESCRIPTOR, CollectionHandleDefaults.of(ObjectBuilder.identity()));

  /** CollectionHandleAsync with no defaults. */
  private static final CollectionHandleAsync<Map<String, Object>> HANDLE_NONE_ASYNC = new CollectionHandleAsync<>(
      null, null,
      DESCRIPTOR, CollectionHandleDefaults.of(ObjectBuilder.identity()));

  /** All defaults are {@code null} if none were set. */
  @Test
  public void test_defaults() {
    Assertions.assertThat(HANDLE_NONE.consistencyLevel()).isNull();
  }

  /**
   * {@link CollectionHandle#withConsistencyLevel} should create a copy with
   * different defaults but not modify the original.
   */
  @Test
  public void test_withConsistencyLevel() {
    var handle = HANDLE_NONE.withConsistencyLevel(ConsistencyLevel.QUORUM);
    Assertions.assertThat(handle.consistencyLevel()).isEqualTo(ConsistencyLevel.QUORUM);
    Assertions.assertThat(HANDLE_NONE.consistencyLevel()).isNull();
  }

  /**
   * {@link CollectionHandleAsync#withConsistencyLevel} should create a copy with
   * different defaults but not modify the original.
   */
  @Test
  public void test_withConsistencyLevel_async() {
    var handle = HANDLE_NONE_ASYNC.withConsistencyLevel(ConsistencyLevel.QUORUM);
    Assertions.assertThat(handle.consistencyLevel()).isEqualTo(ConsistencyLevel.QUORUM);
    Assertions.assertThat(HANDLE_NONE_ASYNC.consistencyLevel()).isNull();
  }
}
