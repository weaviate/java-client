package io.weaviate.client6.v1.api.collections.tenants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.weaviate.client6.v1.api.WeaviateApiException;
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

  /**
   * Add more tenants to the collection.
   *
   * @param tenants Tenant configurations.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void create(Tenant... tenants) throws IOException {
    create(Arrays.asList(tenants));
  }

  /**
   * Add more tenants to the collection.
   *
   * @param tenants Tenant configurations.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void create(List<Tenant> tenants) throws IOException {
    this.restTransport.performRequest(new CreateTenantsRequest(tenants), CreateTenantsRequest.endpoint(collection));
  }

  /**
   * Get tenant information.
   *
   * @param tenant Tenant name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<Tenant> get(String tenant) {
    var tenants = get(List.of(tenant));
    return tenants.isEmpty() ? Optional.empty() : Optional.ofNullable(tenants.get(0));
  }

  /**
   * List all existing tenants.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Tenant> list() {
    return get();
  }

  /**
   * List selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Tenant> get(String... tenants) {
    return get(Arrays.asList(tenants));
  }

  /**
   * List selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public List<Tenant> get(List<String> tenants) {
    return this.grpcTransport.performRequest(new GetTenantsRequest(tenants), GetTenantsRequest.rpc(collection));
  }

  /**
   * Update tenant configuration.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void update(Tenant... tenants) throws IOException {
    update(Arrays.asList(tenants));
  }

  /**
   * Update tenant configuration.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void update(List<Tenant> tenants) throws IOException {
    this.restTransport.performRequest(new UpdateTenantsRequest(tenants), UpdateTenantsRequest.endpoint(collection));
  }

  /**
   * Delete selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(String... tenants) throws IOException {
    delete(Arrays.asList(tenants));
  }

  /**
   * Delete selected tenants.
   *
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(List<String> tenants) throws IOException {
    this.restTransport.performRequest(new DeleteTenantsRequest(tenants), DeleteTenantsRequest.endpoint(collection));
  }

  /**
   * Check if such tenant exists.
   *
   * @param tenant Tenant name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public boolean exists(String tenant) throws IOException {
    return this.restTransport.performRequest(new TenantExistsRequest(tenant), TenantExistsRequest.endpoint(collection));
  }

  /**
   * Activate selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void activate(String... tenants) throws IOException {
    activate(Arrays.asList(tenants));
  }

  /**
   * Activate selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void activate(List<String> tenants) throws IOException {
    update(tenants.stream().map(Tenant::active).toList());
  }

  /**
   * Deactivate selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void deactivate(String... tenants) throws IOException {
    deactivate(Arrays.asList(tenants));
  }

  /**
   * Deactivate selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void deactivate(List<String> tenants) throws IOException {
    update(tenants.stream().map(Tenant::inactive).toList());
  }

  /**
   * Offload selected tenants.
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void offload(String... tenants) throws IOException {
    offload(Arrays.asList(tenants));
  }

  /**
   * Offload selected tenants.
   *
   *
   * @param tenants Tenant names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void offload(List<String> tenants) throws IOException {
    update(tenants.stream().map(t -> new Tenant(t, TenantStatus.OFFLOADED)).toList());
  }
}
