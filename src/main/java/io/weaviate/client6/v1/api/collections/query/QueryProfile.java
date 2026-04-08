package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Map;

/** Per-shard execution timing breakdowns for search queries. */
public record QueryProfile(List<ShardProfile> shards) {
  /**
   * Search profiling samples keyed by shard name.
   * <p>
   * Vector searches:
   *
   * <ul>
   * <li>{@code vector_search_took}: time in the vector index
   * <li>{@code hnsw_flat_search}: "true" / "false", whether flat search was used
   * <li>{@code knn_search_layer_N_took}: per-layer HNSW traversal time
   * <li>{@code knn_search_rescore_took}: PQ/BQ rescore time
   * <li>{@code flat_search_iteration_took}, {@code flat_search_rescore_took}
   * </ul>
   *
   * <p>
   * Keyword (BM25) searches:
   *
   * <ul>
   * <li>{@code kwd_method}, {@code kwd_time}, {@code kwd_filter_size}
   * <li>{@code kwd_1_tok_time}: tokenization time
   * <li>{@code kwd_3_term_time}: term lookup time
   * <li>{@code kwd_4_wand_time / kwd_4_bmw_time}: scoring algorithm time
   * <li>{@code kwd_5_objects_time}: object fetch time
   * <li>{@code kwd_6_res_count}: result count
   * </ul>
   *
   * <p>
   * Filtered searches (any type):
   *
   * <ul>
   * <li>{@code filters_build_allow_list_took}: filter evaluation time
   * <li>{@code filters_ids_matched}: number of IDs matching the filter
   * <li>{@code sort_took}, objects_took
   * </ul>
   */
  public static record ShardProfile(Map<String, Map<String, String>> searches) {
  }
}
