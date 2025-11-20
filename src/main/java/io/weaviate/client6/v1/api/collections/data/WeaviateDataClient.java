package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.api.collections.query.FilterOperand;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClient<PropertiesT> {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;
  private final CollectionDescriptor<PropertiesT> collection;

  private final WeaviateQueryClient<PropertiesT> query;
  private final CollectionHandleDefaults defaults;

  public WeaviateDataClient(
      CollectionDescriptor<PropertiesT> collection,
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
    this.collection = collection;
    this.query = new WeaviateQueryClient<>(collection, grpcTransport, defaults);
    this.defaults = defaults;
  }

  /** Copy constructor that updates the {@link #query} to use new defaults. */
  public WeaviateDataClient(WeaviateDataClient<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.restTransport = c.restTransport;
    this.grpcTransport = c.grpcTransport;
    this.collection = c.collection;
    this.query = new WeaviateQueryClient<>(collection, grpcTransport, defaults);
    this.defaults = defaults;
  }

  public WriteWeaviateObject<PropertiesT> insert(PropertiesT properties) throws IOException {
    return insert(InsertObjectRequest.of(properties));
  }

  public WriteWeaviateObject<PropertiesT> insert(
      PropertiesT properties,
      Function<WriteWeaviateObject.Builder<PropertiesT>, ObjectBuilder<WriteWeaviateObject<PropertiesT>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(properties, fn));
  }

  public WriteWeaviateObject<PropertiesT> insert(InsertObjectRequest<PropertiesT> request)
      throws IOException {
    return this.restTransport.performRequest(request, InsertObjectRequest.endpoint(collection, defaults));
  }

  @SafeVarargs
  public final InsertManyResponse insertMany(PropertiesT... objects) {
    return insertMany(InsertManyRequest.of(objects));
  }

  public InsertManyResponse insertMany(List<WriteWeaviateObject<PropertiesT>> objects) {
    return insertMany(new InsertManyRequest<>(objects));
  }

  @SafeVarargs
  public final InsertManyResponse insertMany(WriteWeaviateObject<PropertiesT>... objects) {
    return insertMany(Arrays.asList(objects));
  }

  public InsertManyResponse insertMany(InsertManyRequest<PropertiesT> request) {
    return this.grpcTransport.performRequest(request,
        InsertManyRequest.rpc(request.objects(), collection, defaults));
  }

  public boolean exists(String uuid) {
    return this.query.fetchObjectById(uuid).isPresent();
  }

  public void update(
      String uuid,
      Function<UpdateObjectRequest.Builder<PropertiesT>, ObjectBuilder<UpdateObjectRequest<PropertiesT>>> fn)
      throws IOException {
    this.restTransport.performRequest(UpdateObjectRequest.of(uuid, fn),
        UpdateObjectRequest.endpoint(collection, defaults));
  }

  public void replace(
      String uuid,
      Function<ReplaceObjectRequest.Builder<PropertiesT>, ObjectBuilder<ReplaceObjectRequest<PropertiesT>>> fn)
      throws IOException {
    this.restTransport.performRequest(ReplaceObjectRequest.of(uuid, fn),
        ReplaceObjectRequest.endpoint(collection, defaults));
  }

  public void delete(String uuid) throws IOException {
    this.restTransport.performRequest(new DeleteObjectRequest(uuid),
        DeleteObjectRequest.endpoint(collection, defaults));
  }

  public DeleteManyResponse deleteMany(String... uuids) {
    var either = Arrays.stream(uuids)
        .map(uuid -> (FilterOperand) Filter.uuid().eq(uuid))
        .toList();
    return deleteMany(DeleteManyRequest.of(Filter.or(either)));
  }

  public DeleteManyResponse deleteMany(Filter filters) {
    return deleteMany(DeleteManyRequest.of(filters));
  }

  public DeleteManyResponse deleteMany(Filter filters,
      Function<DeleteManyRequest.Builder, ObjectBuilder<DeleteManyRequest>> fn) {
    return deleteMany(DeleteManyRequest.of(filters, fn));
  }

  public DeleteManyResponse deleteMany(DeleteManyRequest request) {
    return this.grpcTransport.performRequest(request, DeleteManyRequest.rpc(collection, defaults));
  }

  public void referenceAdd(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collection, defaults));
    }
  }

  public ReferenceAddManyResponse referenceAddMany(BatchReference... references) throws IOException {
    return referenceAddMany(Arrays.asList(references));
  }

  public ReferenceAddManyResponse referenceAddMany(List<BatchReference> references) throws IOException {
    return this.restTransport.performRequest(new ReferenceAddManyRequest(references),
        ReferenceAddManyRequest.endpoint(references, defaults));
  }

  public void referenceDelete(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceDeleteRequest(fromUuid, fromProperty, singleRef),
          ReferenceDeleteRequest.endpoint(collection, defaults));
    }
  }

  public void referenceReplace(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceReplaceRequest(fromUuid, fromProperty, singleRef),
          ReferenceReplaceRequest.endpoint(collection, defaults));
    }
  }
}
