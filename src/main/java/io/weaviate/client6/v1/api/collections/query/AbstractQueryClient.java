package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;

public abstract class AbstractQueryClient<ResponseT, GroupedResponseT> {
  // TODO: collectionName + type + property types should be one object
  // e.g. CollectionDescriptor
  protected final String collectionName;
  protected final GrpcTransport transport;

  AbstractQueryClient(String collectionName, GrpcTransport transport) {
    this.collectionName = collectionName;
    this.transport = transport;
  }

  protected abstract ResponseT performRequest(SearchOperator operator);

  protected abstract GroupedResponseT performRequest(SearchOperator operator, GroupBy groupBy);

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
