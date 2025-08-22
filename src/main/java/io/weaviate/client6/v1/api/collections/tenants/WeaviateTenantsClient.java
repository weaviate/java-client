package io.weaviate.client6.v1.api.collections.tenants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateTenantsClient {
  private final CollectionDescriptor<?> collection;
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateTenantsClient(
      CollectionDescriptor<?> collection,
      RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.collection = collection;
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public void create(Tenant... tenants) throws IOException {
    create(Arrays.asList(tenants));
  }

  public void create(List<Tenant> tenants) throws IOException {
    this.restTransport.performRequest(new CreateTenantsRequest(tenants), CreateTenantsRequest.endpoint(collection));
  }

  public Optional<Tenant> get(String tenant) {
    var tenants = get(List.of(tenant));
    return tenants.isEmpty() ? Optional.empty() : Optional.ofNullable(tenants.get(0));
  }

  public List<Tenant> list() {
    return get();
  }

  public List<Tenant> get(String... tenants) {
    return get(Arrays.asList(tenants));
  }

  public List<Tenant> get(List<String> tenants) {
    return this.grpcTransport.performRequest(new GetTenantsRequest(tenants), GetTenantsRequest.rpc(collection));
  }

  public void update(Tenant... tenants) throws IOException {
    update(Arrays.asList(tenants));
  }

  public void update(List<Tenant> tenants) throws IOException {
    this.restTransport.performRequest(new UpdateTenantsRequest(tenants), UpdateTenantsRequest.endpoint(collection));
  }

  public void delete(String... tenants) throws IOException {
    delete(Arrays.asList(tenants));
  }

  public void delete(List<String> tenants) throws IOException {
    this.restTransport.performRequest(new DeleteTenantsRequest(tenants), DeleteTenantsRequest.endpoint(collection));
  }

  public boolean exists(String tenant) throws IOException {
    return this.restTransport.performRequest(new TenantExistsRequest(tenant), TenantExistsRequest.endpoint(collection));
  }

  public void activate(String... tenants) throws IOException {
    activate(Arrays.asList(tenants));
  }

  public void activate(List<String> tenants) throws IOException {
    update(tenants.stream().map(Tenant::active).toList());
  }

  public void deactivate(String... tenants) throws IOException {
    deactivate(Arrays.asList(tenants));
  }

  public void deactivate(List<String> tenants) throws IOException {
    update(tenants.stream().map(Tenant::inactive).toList());
  }

  public void offload(String... tenants) throws IOException {
    offload(Arrays.asList(tenants));
  }

  public void offload(List<String> tenants) throws IOException {
    update(tenants.stream().map(t -> new Tenant(t, TenantStatus.OFFLOADED)).toList());
  }
}
