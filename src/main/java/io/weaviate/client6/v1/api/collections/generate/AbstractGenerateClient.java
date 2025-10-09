package io.weaviate.client6.v1.api.collections.generate;

import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.Bm25;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.api.collections.query.QueryResponseGrouped;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractGenerateClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> {
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
      AbstractGenerateClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> c,
      CollectionHandleDefaults defaults) {
    this(c.collection, c.grpcTransport, defaults);
  }

  protected abstract ResponseT performRequest(QueryOperator operator, GenerativeTask generate);

  protected abstract GroupedResponseT performRequest(QueryOperator operator, GenerativeTask generate, GroupBy groupBy);

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
   * @param fn         Lambda expression for optional parameters.
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
}
