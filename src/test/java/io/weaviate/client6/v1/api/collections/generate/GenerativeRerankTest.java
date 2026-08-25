package io.weaviate.client6.v1.api.collections.generate;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.api.collections.query.QueryObjectGrouped;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

/**
 * The generative search reuses the query operators, so it is reranked the same
 * way and has to read the scores back the same way.
 */
public class GenerativeRerankTest {

  @Test
  public void test_groupRerankScoreIsUnmarshalled() {
    var reply = WeaviateProtoSearchGet.SearchReply.newBuilder()
        .addGroupByResults(group("fish").setRerank(
            WeaviateProtoSearchGet.RerankReply.newBuilder().setScore(0.42)))
        .build();

    var response = unmarshal(reply);

    Assertions.assertThat(response.groups()).extractingByKey("fish")
        .returns(0.42, GenerativeResponseGroup::rerankScore);
  }

  @Test
  public void test_groupRerankScoreIsAbsentWithoutRerank() {
    var reply = WeaviateProtoSearchGet.SearchReply.newBuilder()
        .addGroupByResults(group("fish"))
        .build();

    var response = unmarshal(reply);

    Assertions.assertThat(response.groups()).extractingByKey("fish")
        .returns(null, GenerativeResponseGroup::rerankScore);
  }

  /** Objects in the group carry their own score, unmarshalled by the query package. */
  @Test
  public void test_objectRerankScoreIsUnmarshalled() {
    var reply = WeaviateProtoSearchGet.SearchReply.newBuilder()
        .addGroupByResults(group("fish"))
        .build();

    var response = unmarshal(reply);

    Assertions.assertThat(response.objects()).first()
        .extracting(QueryObjectGrouped::metadata)
        .returns(0.7811474, QueryMetadata::rerankScore);
  }

  private static WeaviateProtoSearchGet.GroupByResult.Builder group(String name) {
    return WeaviateProtoSearchGet.GroupByResult.newBuilder()
        .setName(name)
        .setNumberOfObjects(1)
        .addObjects(WeaviateProtoSearchGet.SearchResult.newBuilder()
            .setMetadata(WeaviateProtoSearchGet.MetadataResult.newBuilder()
                .setId("d3b07384-d113-4ec4-92e5-1e0f0ab84d43")
                .setRerankScore(0.7811474)
                .setRerankScorePresent(true)));
  }

  private static GenerativeResponseGrouped<Map<String, Object>> unmarshal(
      WeaviateProtoSearchGet.SearchReply reply) {
    return GenerativeResponseGrouped.unmarshal(reply,
        CollectionDescriptor.ofMap("Things"),
        CollectionHandleDefaults.of(ObjectBuilder.identity()));
  }
}
