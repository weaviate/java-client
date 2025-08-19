package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.api.collections.query.Where;
import io.weaviate.client6.v1.api.collections.query.WhereOperand;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClient<PropertiesT> {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;
  private final CollectionDescriptor<PropertiesT> collectionDescriptor;

  private final WeaviateQueryClient<PropertiesT> query;
  private final CollectionHandleDefaults defaults;

  public WeaviateDataClient(
      CollectionDescriptor<PropertiesT> collectionDescriptor,
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
    this.collectionDescriptor = collectionDescriptor;
    this.query = new WeaviateQueryClient<>(collectionDescriptor, grpcTransport, defaults);
    this.defaults = defaults;
  }

  /** Copy constructor that updates the {@link #query} to use new defaults. */
  public WeaviateDataClient(WeaviateDataClient<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.restTransport = c.restTransport;
    this.grpcTransport = c.grpcTransport;
    this.collectionDescriptor = c.collectionDescriptor;
    this.query = new WeaviateQueryClient<>(collectionDescriptor, grpcTransport, defaults);
    this.defaults = defaults;
  }

  public WeaviateObject<PropertiesT, Object, ObjectMetadata> insert(PropertiesT properties) throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties));
  }

  public WeaviateObject<PropertiesT, Object, ObjectMetadata> insert(PropertiesT properties,
      Function<InsertObjectRequest.Builder<PropertiesT>, ObjectBuilder<InsertObjectRequest<PropertiesT>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties, fn));
  }

  @SafeVarargs
  public final InsertManyResponse insertMany(PropertiesT... objects) {
    return insertMany(InsertManyRequest.of(objects));
  }

  public InsertManyResponse insertMany(List<WeaviateObject<PropertiesT, Reference, ObjectMetadata>> objects) {
    return insertMany(new InsertManyRequest<>(objects));
  }

  public InsertManyResponse insertMany(InsertManyRequest<PropertiesT> request) {
    return this.grpcTransport.performRequest(request,
        InsertManyRequest.rpc(request.objects(), collectionDescriptor, defaults));
  }

  public WeaviateObject<PropertiesT, Object, ObjectMetadata> insert(InsertObjectRequest<PropertiesT> request)
      throws IOException {
    return this.restTransport.performRequest(request, InsertObjectRequest.endpoint(collectionDescriptor));
  }

  public boolean exists(String uuid) throws IOException {
    return this.query.byId(uuid).isPresent();
  }

  public void update(String uuid,
      Function<UpdateObjectRequest.Builder<PropertiesT>, ObjectBuilder<UpdateObjectRequest<PropertiesT>>> fn)
      throws IOException {
    this.restTransport.performRequest(UpdateObjectRequest.of(collectionDescriptor.name(), uuid, fn),
        UpdateObjectRequest.endpoint(collectionDescriptor));
  }

  public void replace(String uuid,
      Function<ReplaceObjectRequest.Builder<PropertiesT>, ObjectBuilder<ReplaceObjectRequest<PropertiesT>>> fn)
      throws IOException {
    this.restTransport.performRequest(ReplaceObjectRequest.of(collectionDescriptor.name(), uuid, fn),
        ReplaceObjectRequest.endpoint(collectionDescriptor));
  }

  public void delete(String uuid) throws IOException {
    this.restTransport.performRequest(new DeleteObjectRequest(collectionDescriptor.name(), uuid),
        DeleteObjectRequest._ENDPOINT);
  }

  public DeleteManyResponse deleteMany(String... uuids) throws IOException {
    var either = Arrays.stream(uuids)
        .map(uuid -> (WhereOperand) Where.uuid().eq(uuid))
        .toList();
    return deleteMany(DeleteManyRequest.of(Where.or(either)));
  }

  public DeleteManyResponse deleteMany(Where where) throws IOException {
    return deleteMany(DeleteManyRequest.of(where));
  }

  public DeleteManyResponse deleteMany(Where where,
      Function<DeleteManyRequest.Builder, ObjectBuilder<DeleteManyRequest>> fn)
      throws IOException {
    return deleteMany(DeleteManyRequest.of(where, fn));
  }

  public DeleteManyResponse deleteMany(DeleteManyRequest request) throws IOException {
    return this.grpcTransport.performRequest(request, DeleteManyRequest.rpc(collectionDescriptor, defaults));
  }

  public void referenceAdd(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collectionDescriptor));
    }
  }

  public ReferenceAddManyResponse referenceAddMany(BatchReference... references) throws IOException {
    return referenceAddMany(Arrays.asList(references));
  }

  public ReferenceAddManyResponse referenceAddMany(List<BatchReference> references) throws IOException {
    return this.restTransport.performRequest(new ReferenceAddManyRequest(references),
        ReferenceAddManyRequest.endpoint(references));
  }

  public void referenceDelete(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceDeleteRequest(fromUuid, fromProperty, singleRef),
          ReferenceDeleteRequest.endpoint(collectionDescriptor));
    }
  }

  public void referenceReplace(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceReplaceRequest(fromUuid, fromProperty, singleRef),
          ReferenceReplaceRequest.endpoint(collectionDescriptor));
    }
  }
}
