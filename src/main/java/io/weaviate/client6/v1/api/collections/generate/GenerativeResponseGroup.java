package io.weaviate.client6.v1.api.collections.generate;

import java.util.List;

import io.weaviate.client6.v1.api.collections.query.BaseQueryOptions;
import io.weaviate.client6.v1.api.collections.query.QueryObjectGrouped;
import io.weaviate.client6.v1.api.collections.query.Rerank;

public record GenerativeResponseGroup<PropertiesT>(
    /** Group name. */
    String name,
    /**
     * The smallest distance value among all objects in the group, indicating the
     * most similar object in that group to the query
     */
    Float minDistance,
    /**
     * The largest distance value among all objects in the group, indicating the
     * least similar object in that group to the query.
     */
    Float maxDistance,
    /** The size of the group. */
    long numberOfObjects,
    /**
     * Score assigned to this group by the reranker module.
     *
     * <p>
     * Only present if the query requested reranking, see
     * {@link BaseQueryOptions.Builder#rerank(Rerank)}.
     */
    Double rerankScore,
    /** Objects retrieved in the query. */
    List<QueryObjectGrouped<PropertiesT>> objects,
    /** Output of the summary task for this group. */
    TaskOutput generative) {
}
