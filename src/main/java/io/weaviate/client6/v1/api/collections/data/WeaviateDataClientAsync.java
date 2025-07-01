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

public class WeaviateDataClientAsync<T> {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;
  private final CollectionDescriptor<T> collectionDescriptor;

  private final WeaviateQueryClientAsync<T> query;

  public WeaviateDataClientAsync(CollectionDescriptor<T> collectionDescriptor, RestTransport restTransport,
      GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
    this.collectionDescriptor = collectionDescriptor;
    this.query = new WeaviateQueryClientAsync<>(collectionDescriptor, grpcTransport);
  }

  public CompletableFuture<WeaviateObject<T, Object, ObjectMetadata>> insert(T properties) throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties));
  }

  public CompletableFuture<WeaviateObject<T, Object, ObjectMetadata>> insert(T properties,
      Function<InsertObjectRequest.Builder<T>, ObjectBuilder<InsertObjectRequest<T>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties, fn));
  }

  public CompletableFuture<WeaviateObject<T, Object, ObjectMetadata>> insert(InsertObjectRequest<T> request)
      throws IOException {
    return this.restTransport.performRequestAsync(request, InsertObjectRequest.endpoint(collectionDescriptor));
  }

  @SafeVarargs
  public final CompletableFuture<InsertManyResponse> insertMany(T... objects) {
    return insertMany(InsertManyRequest.of(objects));
  }

  public CompletableFuture<InsertManyResponse> insertMany(List<WeaviateObject<T, Reference, ObjectMetadata>> objects) {
    return insertMany(new InsertManyRequest<>(objects));
  }

  public CompletableFuture<InsertManyResponse> insertMany(InsertManyRequest<T> request) {
    return this.grpcTransport.performRequestAsync(request,
        InsertManyRequest.rpc(request.objects(), collectionDescriptor));
  }

  public CompletableFuture<Boolean> exists(String uuid) {
    return this.query.byId(uuid).thenApply(Optional::isPresent);
  }

  public CompletableFuture<Void> update(String uuid,
      Function<UpdateObjectRequest.Builder<T>, ObjectBuilder<UpdateObjectRequest<T>>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(UpdateObjectRequest.of(collectionDescriptor.name(), uuid, fn),
        UpdateObjectRequest.endpoint(collectionDescriptor));
  }

  public CompletableFuture<Void> replace(String uuid,
      Function<ReplaceObjectRequest.Builder<T>, ObjectBuilder<ReplaceObjectRequest<T>>> fn)
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
