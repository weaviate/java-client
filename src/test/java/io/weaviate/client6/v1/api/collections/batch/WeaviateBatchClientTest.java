package io.weaviate.client6.v1.api.collections.batch;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.testutil.transport.MockGrpcTransport;

public class WeaviateBatchClientTest {
  @Test
  public void test_start_rejectsServerBefore136() {
    var batch = new WeaviateBatchClient<Map<String, Object>>(
        new MockGrpcTransport("1.35.6"),
        CollectionDescriptor.ofMap("Things"),
        CollectionHandleDefaults.of(CollectionHandleDefaults.none()));

    Assertions.assertThatThrownBy(batch::start)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("1.36")
        .hasMessageContaining("1.35.6");
  }
}
