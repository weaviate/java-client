package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClientAsync;
import io.weaviate.client6.v1.api.collections.query.Where;
import io.weaviate.client6.v1.api.collections.query.WhereOperand;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClientAsync<PropertiesT> {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;
  private final CollectionDescriptor<PropertiesT> collectionDescriptor;

  private final WeaviateQueryClientAsync<PropertiesT> query;

  public WeaviateDataClientAsync(CollectionDescriptor<PropertiesT> collectionDescriptor, RestTransport restTransport,
      GrpcTransport grpcTransport, WeaviateQueryClientAsync<PropertiesT> query) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
    this.collectionDescriptor = collectionDescriptor;
    this.query = query;
  }

  /** Copy constructor that updates the {@link #query} to use new defaults. */
  public WeaviateDataClientAsync(WeaviateDataClientAsync<PropertiesT> c, WeaviateQueryClientAsync<PropertiesT> query) {
    this.restTransport = c.restTransport;
    this.grpcTransport = c.grpcTransport;
    this.collectionDescriptor = c.collectionDescriptor;
    this.query = query;
  }

  public CompletableFuture<WeaviateObject<PropertiesT, Object, ObjectMetadata>> insert(PropertiesT properties)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties));
  }

  public CompletableFuture<WeaviateObject<PropertiesT, Object, ObjectMetadata>> insert(PropertiesT properties,
      Function<InsertObjectRequest.Builder<PropertiesT>, ObjectBuilder<InsertObjectRequest<PropertiesT>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties, fn));
  }

  public CompletableFuture<WeaviateObject<PropertiesT, Object, ObjectMetadata>> insert(
      InsertObjectRequest<PropertiesT> request)
      throws IOException {
    return this.restTransport.performRequestAsync(request, InsertObjectRequest.endpoint(collectionDescriptor));
  }

  @SafeVarargs
  public final CompletableFuture<InsertManyResponse> insertMany(PropertiesT... objects) {
    return insertMany(InsertManyRequest.of(objects));
  }

  public CompletableFuture<InsertManyResponse> insertMany(
      List<WeaviateObject<PropertiesT, Reference, ObjectMetadata>> objects) {
    return insertMany(new InsertManyRequest<>(objects));
  }

  public CompletableFuture<InsertManyResponse> insertMany(InsertManyRequest<PropertiesT> request) {
    return this.grpcTransport.performRequestAsync(request,
        InsertManyRequest.rpc(request.objects(), collectionDescriptor));
  }

  public CompletableFuture<Boolean> exists(String uuid) {
    return this.query.byId(uuid).thenApply(Optional::isPresent);
  }

  public CompletableFuture<Void> update(String uuid,
      Function<UpdateObjectRequest.Builder<PropertiesT>, ObjectBuilder<UpdateObjectRequest<PropertiesT>>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(UpdateObjectRequest.of(collectionDescriptor.name(), uuid, fn),
        UpdateObjectRequest.endpoint(collectionDescriptor));
  }

  public CompletableFuture<Void> replace(String uuid,
      Function<ReplaceObjectRequest.Builder<PropertiesT>, ObjectBuilder<ReplaceObjectRequest<PropertiesT>>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(ReplaceObjectRequest.of(collectionDescriptor.name(), uuid, fn),
        ReplaceObjectRequest.endpoint(collectionDescriptor));
  }

  public CompletableFuture<Void> delete(String uuid) {
    return this.restTransport.performRequestAsync(new DeleteObjectRequest(collectionDescriptor.name(), uuid),
        DeleteObjectRequest._ENDPOINT);
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(String... uuids) throws IOException {
    var either = Arrays.stream(uuids)
        .map(uuid -> (WhereOperand) Where.uuid().eq(uuid))
        .toList();
    return deleteMany(DeleteManyRequest.of(Where.or(either)));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(Where where) throws IOException {
    return deleteMany(DeleteManyRequest.of(where));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(Where where,
      Function<DeleteManyRequest.Builder, ObjectBuilder<DeleteManyRequest>> fn)
      throws IOException {
    return deleteMany(DeleteManyRequest.of(where, fn));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(DeleteManyRequest request) throws IOException {
    return this.grpcTransport.performRequestAsync(request, DeleteManyRequest.rpc(collectionDescriptor));
  }

  public CompletableFuture<Void> referenceAdd(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collectionDescriptor));
    });
  }

  public CompletableFuture<ReferenceAddManyResponse> referenceAddMany(BatchReference... references) throws IOException {
    return referenceAddMany(Arrays.asList(references));
  }

  public CompletableFuture<ReferenceAddManyResponse> referenceAddMany(List<BatchReference> references)
      throws IOException {
    return this.restTransport.performRequestAsync(new ReferenceAddManyRequest(references),
        ReferenceAddManyRequest.endpoint(references));
  }

  public CompletableFuture<Void> referenceDelete(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceDeleteRequest(fromUuid, fromProperty, singleRef),
          ReferenceDeleteRequest.endpoint(collectionDescriptor));
    });
  }

  public CompletableFuture<Void> referenceReplace(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceReplaceRequest(fromUuid, fromProperty, singleRef),
          ReferenceReplaceRequest.endpoint(collectionDescriptor));
    });
  }

  /**
   * Spawn execution {@code fn} for each of the {@code elements} and return a
   * flattened {@link CompletableFuture#allOf}.
   *
   * <p>
   * Usage:
   *
   * <pre>{@code
   *  // With elements immediately available
   *  forEachAsync(myElements, element -> doNetworkIo(element));
   *
   *  // Chain to another CompletableFuture
   *  fetch(request).thenCompose(elements -> forEachAsync(...));
   * }</pre>
   */
  private static <T> CompletableFuture<Void> forEachAsync(Collection<T> elements,
      Function<T, CompletableFuture<?>> fn) {
    var futures = elements.stream().map(el -> fn.apply(el))
        .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(futures);
  }
}
