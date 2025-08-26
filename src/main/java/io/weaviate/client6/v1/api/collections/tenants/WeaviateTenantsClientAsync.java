package io.weaviate.client6.v1.api.collections.tenants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateTenantsClientAsync {
  private final CollectionDescriptor<?> collection;
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateTenantsClientAsync(
      CollectionDescriptor<?> collection,
      RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.collection = collection;
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public CompletableFuture<Void> create(Tenant... tenants) throws IOException {
    return create(Arrays.asList(tenants));
  }

  public CompletableFuture<Void> create(List<Tenant> tenants) throws IOException {
    return this.restTransport.performRequestAsync(new CreateTenantsRequest(tenants),
        CreateTenantsRequest.endpoint(collection));
  }

  public CompletableFuture<Optional<Tenant>> get(String tenant) {
    var tenants = get(List.of(tenant));
    return tenants.thenApply(result -> result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.get(0)));
  }

  public CompletableFuture<List<Tenant>> list() {
    return get();
  }

  public CompletableFuture<List<Tenant>> get(String... tenants) {
    return get(Arrays.asList(tenants));
  }

  public CompletableFuture<List<Tenant>> get(List<String> tenants) {
    return this.grpcTransport.performRequestAsync(new GetTenantsRequest(tenants), GetTenantsRequest.rpc(collection));
  }

  public CompletableFuture<Void> update(Tenant... tenants) throws IOException {
    return update(Arrays.asList(tenants));
  }

  public CompletableFuture<Void> update(List<Tenant> tenants) throws IOException {
    return this.restTransport.performRequestAsync(new UpdateTenantsRequest(tenants),
        UpdateTenantsRequest.endpoint(collection));
  }

  public CompletableFuture<Void> delete(String... tenants) throws IOException {
    return delete(Arrays.asList(tenants));
  }

  public CompletableFuture<Void> delete(List<String> tenants) throws IOException {
    return this.restTransport.performRequestAsync(new DeleteTenantsRequest(tenants),
        DeleteTenantsRequest.endpoint(collection));
  }

  public CompletableFuture<Boolean> exists(String tenant) throws IOException {
    return this.restTransport.performRequestAsync(new TenantExistsRequest(tenant),
        TenantExistsRequest.endpoint(collection));
  }

  public CompletableFuture<Void> activate(String... tenants) throws IOException {
    return activate(Arrays.asList(tenants));
  }

  public CompletableFuture<Void> activate(List<String> tenants) throws IOException {
    return update(tenants.stream().map(Tenant::active).toList());
  }

  public CompletableFuture<Void> deactivate(String... tenants) throws IOException {
    return deactivate(Arrays.asList(tenants));
  }

  public CompletableFuture<Void> deactivate(List<String> tenants) throws IOException {
    return update(tenants.stream().map(Tenant::inactive).toList());
  }

  public CompletableFuture<Void> offload(String... tenants) throws IOException {
    return offload(Arrays.asList(tenants));
  }

  public CompletableFuture<Void> offload(List<String> tenants) throws IOException {
    return update(tenants.stream().map(t -> new Tenant(t, TenantStatus.OFFLOADED)).toList());
  }
}
