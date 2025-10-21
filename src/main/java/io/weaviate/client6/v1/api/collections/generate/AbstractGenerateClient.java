package io.weaviate.client6.v1.api.collections.generate;

import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.Bm25;
import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.Hybrid;
import io.weaviate.client6.v1.api.collections.query.NearVector;
import io.weaviate.client6.v1.api.collections.query.NearVectorTarget;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.api.collections.query.QueryResponseGrouped;
import io.weaviate.client6.v1.api.collections.query.Target;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractGenerateClient<PropertiesT, ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<PropertiesT> collection;
  protected final GrpcTransport grpcTransport;
  protected final CollectionHandleDefaults defaults;

  AbstractGenerateClient(CollectionDescriptor<PropertiesT> collection, GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.collection = collection;
    this.grpcTransport = grpcTransport;
    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  AbstractGenerateClient(
      AbstractGenerateClient<PropertiesT, ResponseT, GroupedResponseT> c,
      CollectionHandleDefaults defaults) {
    this(c.collection, c.grpcTransport, defaults);
  }

  protected abstract ResponseT performRequest(QueryOperator operator, GenerativeTask generate);

  protected abstract GroupedResponseT performRequest(QueryOperator operator, GenerativeTask generate, GroupBy groupBy);

  // Object queries -----------------------------------------------------------

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return fetchObjects(FetchObjects.of(fn), GenerativeTask.of(generateFn));
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param query    FetchObjects query.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT fetchObjects(FetchObjects query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   *
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return fetchObjects(FetchObjects.of(fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param query    FetchObjects query.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT fetchObjects(FetchObjects query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }
  // BM25 queries -------------------------------------------------------------

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return bm25(Bm25.of(query), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(
      String query,
      Function<Bm25.Builder, ObjectBuilder<Bm25>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return bm25(Bm25.of(query, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query    BM25 query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(Bm25 query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return bm25(Bm25.of(query), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(String query,
      Function<Bm25.Builder, ObjectBuilder<Bm25>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return bm25(Bm25.of(query, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query    BM25 query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(Bm25 query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // Hybrid queries -----------------------------------------------------------

  /**
   * Query collection objects using hybrid search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(query), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(
      String query,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(query, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param searchTarget Query target.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(
      Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(
      Target searchTarget,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query    Hybrid query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(Hybrid query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(
      String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(query), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(
      String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param searchTarget Query target.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(
      Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(
      Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query    Query string.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(Hybrid query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearVector queries -------------------------------------------------------

  /**
   * Query collection objects using near vector search.
   *
   * @param vector     Query vector.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(float[] vector,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(Target.vector(vector), generateFn);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector     Query vector.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(Target.vector(vector), fn, generateFn);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVectorTarget searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(NearVector.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVectorTarget searchTarget,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(NearVector.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param query    Near vector query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVector query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector     Query vector.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(Target.vector(vector), generateFn, groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector     Query vector.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(Target.vector(vector), fn, generateFn, groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(
      NearVectorTarget searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(NearVectorTarget searchTarget,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param query    Near vector query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(NearVector query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }
}
