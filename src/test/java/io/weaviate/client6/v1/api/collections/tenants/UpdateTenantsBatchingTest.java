package io.weaviate.client6.v1.api.collections.tenants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.testutil.transport.MockRestTransport;

/**
 * The server rejects an update of more than 100 tenants with HTTP 422, so the
 * client splits longer lists. Adding tenants is not capped and has to keep going
 * out as a single request.
 */
public class UpdateTenantsBatchingTest {
  private static final CollectionDescriptor<?> COLLECTION = CollectionDescriptor.ofMap("Things");

  private static List<Tenant> tenants(int n) {
    return IntStream.rangeClosed(1, n).mapToObj(i -> Tenant.active("tenant-" + i)).toList();
  }

  private static List<String> names(int n) {
    return IntStream.rangeClosed(1, n).mapToObj(i -> "tenant-" + i).toList();
  }

  /** Collect the body of every request the client sent, in order. */
  private static List<String> bodies(MockRestTransport transport, int expectAtMost) {
    var seen = new ArrayList<String>();
    var assertions = new MockRestTransport.AssertFunction[expectAtMost];
    for (var i = 0; i < expectAtMost; i++) {
      assertions[i] = (method, url, body, query) -> seen.add(body);
    }
    transport.assertNext(assertions);
    return seen;
  }

  @Test
  public void test_batchesOfAtMost100() {
    Assertions.assertThat(UpdateTenantsRequest.batches(tenants(100)))
        .as("exactly at the limit is one request").hasSize(1);
    Assertions.assertThat(UpdateTenantsRequest.batches(tenants(101)))
        .as("one over the limit splits")
        .extracting(List::size).containsExactly(100, 1);
    Assertions.assertThat(UpdateTenantsRequest.batches(tenants(250)))
        .extracting(List::size).containsExactly(100, 100, 50);
  }

  @Test
  public void test_batchesCoverEveryTenantInOrder() {
    var all = tenants(250);

    var flattened = new ArrayList<Tenant>();
    UpdateTenantsRequest.batches(all).forEach(flattened::addAll);

    Assertions.assertThat(flattened).isEqualTo(all);
  }

  @Test
  public void test_emptyListIsASingleEmptyBatch() {
    Assertions.assertThat(UpdateTenantsRequest.batches(List.of())).containsExactly(List.of());
  }

  @Test
  public void test_updateSplitsIntoSeveralRequests() throws IOException {
    var transport = new MockRestTransport();

    new WeaviateTenantsClient(COLLECTION, transport, null).update(tenants(101));

    var bodies = bodies(transport, 3);
    Assertions.assertThat(bodies).as("101 tenants -> 2 requests").hasSize(2);
    Assertions.assertThat(bodies.get(0)).contains("tenant-100").doesNotContain("tenant-101");
    Assertions.assertThat(bodies.get(1)).contains("tenant-101").doesNotContain("tenant-100");
  }

  @Test
  public void test_updateAtTheLimitIsASingleRequest() throws IOException {
    var transport = new MockRestTransport();

    new WeaviateTenantsClient(COLLECTION, transport, null).update(tenants(100));

    Assertions.assertThat(bodies(transport, 2)).hasSize(1);
  }

  /** activate/deactivate/offload all delegate to update, so they split too. */
  @Test
  public void test_deactivateSplitsIntoSeveralRequests() throws IOException {
    var transport = new MockRestTransport();

    new WeaviateTenantsClient(COLLECTION, transport, null).deactivate(names(201));

    var bodies = bodies(transport, 4);
    Assertions.assertThat(bodies).as("201 tenants -> 3 requests").hasSize(3);
    Assertions.assertThat(bodies).allSatisfy(body -> Assertions.assertThat(body).contains("INACTIVE"));
  }

  @Test
  public void test_createIsNotSplit() throws IOException {
    var transport = new MockRestTransport();

    new WeaviateTenantsClient(COLLECTION, transport, null).create(tenants(250));

    var bodies = bodies(transport, 2);
    Assertions.assertThat(bodies).as("adding tenants is not capped by the server").hasSize(1);
    Assertions.assertThat(bodies.get(0)).contains("tenant-1").contains("tenant-250");
  }

  @Test
  public void test_asyncUpdateSplitsIntoSeveralRequests() throws Exception {
    var transport = new MockRestTransport();

    new WeaviateTenantsClientAsync(COLLECTION, transport, null).update(tenants(101)).get();

    var bodies = bodies(transport, 3);
    Assertions.assertThat(bodies).hasSize(2);
    Assertions.assertThat(bodies.get(1)).contains("tenant-101");
  }
}
