package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractQueryClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<PropertiesT> collection;
  protected final GrpcTransport transport;

  AbstractQueryClient(CollectionDescriptor<PropertiesT> collection, GrpcTransport transport) {
    this.collection = collection;
    this.transport = transport;
  }

  protected abstract SingleT byId(ById byId);

  protected abstract ResponseT performRequest(SearchOperator operator);

  protected abstract GroupedResponseT performRequest(SearchOperator operator, GroupBy groupBy);

  // Fetch by ID --------------------------------------------------------------

  public SingleT byId(String uuid) {
    return byId(ById.of(uuid));
  }

  public SingleT byId(String uuid, Function<ById.Builder, ObjectBuilder<ById>> fn) {
    return byId(ById.of(uuid, fn));
  }

  protected final <T> Optional<T> optionalFirst(List<T> objects) {
    return objects.isEmpty() ? Optional.empty() : Optional.ofNullable(objects.get(0));
  }

  // Object queries -----------------------------------------------------------

  public ResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn) {
    return fetchObjects(FetchObjects.of(fn));
  }

  public ResponseT fetchObjects(FetchObjects query) {
    return performRequest(query);
  }

  public GroupedResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      GroupBy groupBy) {
    return fetchObjects(FetchObjects.of(fn), groupBy);
  }

  public GroupedResponseT fetchObjects(FetchObjects query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearVector queries -------------------------------------------------------

  public ResponseT nearVector(Float[] vector) {
    return nearVector(NearVector.of(vector));
  }

  public ResponseT nearVector(Float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn) {
    return nearVector(NearVector.of(vector, fn));
  }

  public ResponseT nearVector(NearVector query) {
    return performRequest(query);
  }

  public GroupedResponseT nearVector(Float[] vector, GroupBy groupBy) {
    return nearVector(NearVector.of(vector), groupBy);
  }

  public GroupedResponseT nearVector(Float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(vector, fn), groupBy);
  }

  public GroupedResponseT nearVector(NearVector query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearText queries ---------------------------------------------------------

  public ResponseT nearText(String... text) {
    return nearText(NearText.of(text));
  }

  public ResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  public ResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  public ResponseT nearText(NearText query) {
    return performRequest(query);
  }

  public GroupedResponseT nearText(String text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(List<String> text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(NearText query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearImage queries --------------------------------------------------------

  public ResponseT nearImage(String image) {
    return nearImage(NearImage.of(image));
  }

  public ResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn) {
    return nearImage(NearImage.of(image, fn));
  }

  public ResponseT nearImage(NearImage query) {
    return performRequest(query);
  }

  public GroupedResponseT nearImage(String image, GroupBy groupBy) {
    return nearImage(NearImage.of(image), groupBy);
  }

  public GroupedResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(image, fn), groupBy);
  }

  public GroupedResponseT nearImage(NearImage query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }
}
