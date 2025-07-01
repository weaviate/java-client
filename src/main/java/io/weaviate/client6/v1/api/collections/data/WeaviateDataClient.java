package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.api.collections.query.Where;
import io.weaviate.client6.v1.api.collections.query.WhereOperand;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClient<T> {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;
  private final CollectionDescriptor<T> collectionDescriptor;

  private final WeaviateQueryClient<T> query;

  public WeaviateDataClient(CollectionDescriptor<T> collectionDescriptor, RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
    this.collectionDescriptor = collectionDescriptor;
    this.query = new WeaviateQueryClient<>(collectionDescriptor, grpcTransport);

  }

  public WeaviateObject<T, Object, ObjectMetadata> insert(T properties) throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties));
  }

  public WeaviateObject<T, Object, ObjectMetadata> insert(T properties,
      Function<InsertObjectRequest.Builder<T>, ObjectBuilder<InsertObjectRequest<T>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties, fn));
  }

  @SafeVarargs
  public final InsertManyResponse insertMany(T... objects) {
    return insertMany(InsertManyRequest.of(objects));
  }

  public InsertManyResponse insertMany(List<WeaviateObject<T, Reference, ObjectMetadata>> objects) {
    return insertMany(new InsertManyRequest<>(objects));
  }

  public InsertManyResponse insertMany(InsertManyRequest<T> request) {
    return this.grpcTransport.performRequest(request, InsertManyRequest.rpc(request.objects(), collectionDescriptor));
  }

  public WeaviateObject<T, Object, ObjectMetadata> insert(InsertObjectRequest<T> request) throws IOException {
    return this.restTransport.performRequest(request, InsertObjectRequest.endpoint(collectionDescriptor));
  }

  public boolean exists(String uuid) throws IOException {
    return this.query.byId(uuid).isPresent();
  }

  public void update(String uuid, Function<UpdateObjectRequest.Builder<T>, ObjectBuilder<UpdateObjectRequest<T>>> fn)
      throws IOException {
    this.restTransport.performRequest(UpdateObjectRequest.of(collectionDescriptor.name(), uuid, fn),
        UpdateObjectRequest.endpoint(collectionDescriptor));
  }

  public void replace(String uuid, Function<ReplaceObjectRequest.Builder<T>, ObjectBuilder<ReplaceObjectRequest<T>>> fn)
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
    return this.grpcTransport.performRequest(request, DeleteManyRequest.rpc(collectionDescriptor));
  }

  public void referenceAdd(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collectionDescriptor));
    }
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
