package io.weaviate.client6.v1.api.collections.generate;

import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.Bm25;
import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.Hybrid;
import io.weaviate.client6.v1.api.collections.query.NearObject;
import io.weaviate.client6.v1.api.collections.query.NearText;
import io.weaviate.client6.v1.api.collections.query.NearVector;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.api.collections.query.QueryResponseGrouped;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractQueryClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<PropertiesT> collection;
  protected final GrpcTransport grpcTransport;
  protected final CollectionHandleDefaults defaults;

  AbstractQueryClient(CollectionDescriptor<PropertiesT> collection, GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.collection = collection;
    this.grpcTransport = grpcTransport;
    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  AbstractQueryClient(
      AbstractQueryClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> c,
      CollectionHandleDefaults defaults) {
    this(c.collection, c.grpcTransport, defaults);
  }

  protected abstract ResponseT performRequest(QueryOperator operator);

  protected abstract GroupedResponseT performRequest(QueryOperator operator, GroupBy groupBy);

  // Object queries -----------------------------------------------------------

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param fn Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT fetchObjects(Function<Generate.FetchObjects, ObjectBuilder<FetchObjects>> fn) {
    return fetchObjects(Generate.fetchObjects(fn));
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param query FetchObjects query.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT fetchObjects(FetchObjects query) {
    return performRequest(query);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   *
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT fetchObjects(Function<Generate.FetchObjects, ObjectBuilder<FetchObjects>> fn,
      GroupBy groupBy) {
    return fetchObjects(Generate.fetchObjects(fn), groupBy);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param query   FetchObjects query.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT fetchObjects(FetchObjects query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // BM25 queries -------------------------------------------------------------

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query Query string.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(String query) {
    return bm25(Bm25.of(query));
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query Query string.
   * @param fn    Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(String query, Function<Bm25.Builder, ObjectBuilder<Bm25>> fn) {
    return bm25(Bm25.of(query, fn));
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query BM25 query request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(Bm25 query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query   Query string.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(String query, GroupBy groupBy) {
    return bm25(Bm25.of(query), groupBy);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query   Query string.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(String query, Function<Bm25.Builder, ObjectBuilder<Bm25>> fn, GroupBy groupBy) {
    return bm25(Bm25.of(query, fn), groupBy);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query   BM25 query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(Bm25 query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // Hybrid queries -----------------------------------------------------------

  /**
   * Query collection objects using hybrid search.
   *
   * @param query Query string.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(String query) {
    return hybrid(Hybrid.of(query));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query Query string.
   * @param fn    Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn) {
    return hybrid(Hybrid.of(query, fn));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query Hybrid query request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(Hybrid query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query   Query string.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(String query, GroupBy groupBy) {
    return hybrid(Hybrid.of(query), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query   Query string.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, fn), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query   Query string.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(Hybrid query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearVector queries -------------------------------------------------------

  /**
   * Query collection objects using near vector search.
   *
   * @param vector Query vector.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(float[] vector) {
    return nearVector(NearVector.of(vector));
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector Query vector.
   * @param fn     Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn) {
    return nearVector(NearVector.of(vector, fn));
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param query Near vector query request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVector query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector  Query vector.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector, GroupBy groupBy) {
    return nearVector(NearVector.of(vector), groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector  Query vector.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(vector, fn), groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param query   Near vector query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(NearVector query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearObject queries -------------------------------------------------------

  /**
   * Query collection objects using near object search.
   *
   * @param uuid Query object UUID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearObject(String uuid) {
    return nearObject(NearObject.of(uuid));
  }

  /**
   * Query collection objects using near object search.
   *
   * @param uuid Query object UUID.
   * @param fn   Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> fn) {
    return nearObject(NearObject.of(uuid, fn));
  }

  /**
   * Query collection objects using near object search.
   *
   * @param query Near object query request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearObject(NearObject query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near object search.
   *
   * @param uuid    Query object UUID.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid, GroupBy groupBy) {
    return nearObject(NearObject.of(uuid), groupBy);
  }

  /**
   * Query collection objects using near object search.
   *
   * @param uuid    Query object UUID.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> fn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid, fn), groupBy);
  }

  /**
   * Query collection objects using near object search.
   *
   * @param query   Near object query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearObject(NearObject query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearText queries ---------------------------------------------------------

  /**
   * Query collection objects using near text search.
   *
   * @param text Query concepts.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(String... text) {
    return nearText(NearText.of(text));
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text Query concepts.
   * @param fn   Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text Query concepts.
   * @param fn   Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  /**
   * Query collection objects using near text search.
   *
   * @param query Near text query request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(NearText query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(String text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(List<String> text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param query   Near text query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(NearText query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }
}
