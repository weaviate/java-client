package io.weaviate.client6.v1.api.collections.query;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

@RunWith(JParamsTestRunner.class)
public class RerankTest {
  private static final Rerank RERANK = Rerank.by("title", rank -> rank.query("fish"));

  /** Every search operator can be reranked, not only the vector searches. */
  public static Object[][] operators() {
    return new Object[][] {
        { "bm25", Bm25.of("animal", q -> q.rerank(RERANK)) },
        { "hybrid", Hybrid.of("animal", q -> q.rerank(RERANK)) },
        { "fetchObjects", FetchObjects.of(q -> q.rerank(RERANK)) },
        { "nearText", NearText.of("animal", q -> q.rerank(RERANK)) },
        { "nearVector", NearVector.of(new float[] { 1, 2 }, q -> q.rerank(RERANK)) },
        { "nearObject", NearObject.of("d3b07384-d113-4ec4-92e5-1e0f0ab84d43", q -> q.rerank(RERANK)) },
    };
  }

  @Name("{0}")
  @DataMethod(source = RerankTest.class, method = "operators")
  @Test
  public void test_rerankIsMarshalled(String __, QueryOperator operator) {
    var request = marshal(operator);

    Assertions.assertThat(request.hasRerank()).as("has rerank").isTrue();
    Assertions.assertThat(request.getRerank().getProperty()).isEqualTo("title");
    Assertions.assertThat(request.getRerank().getQuery()).isEqualTo("fish");
    Assertions.assertThat(operator.rerank()).as("readable back off the operator").isEqualTo(RERANK);
  }

  @Test
  public void test_noRerankByDefault() {
    Assertions.assertThat(marshal(Bm25.of("animal")).hasRerank()).isFalse();
    Assertions.assertThat(Bm25.of("animal").rerank()).isNull();
  }

  @Test
  public void test_rerankWithoutQuery() {
    var request = marshal(Bm25.of("animal", q -> q.rerank(Rerank.by("title"))));

    Assertions.assertThat(request.getRerank().getProperty()).isEqualTo("title");
    Assertions.assertThat(request.getRerank().hasQuery()).as("query is optional").isFalse();
  }

  @Test
  public void test_rerankScoreIsUnmarshalled() {
    var reply = WeaviateProtoSearchGet.SearchReply.newBuilder()
        .addResults(searchResult(0.7811474))
        .build();

    var response = QueryResponse.unmarshal(reply, CollectionDescriptor.ofMap("Things"));

    Assertions.assertThat(response.objects()).first()
        .extracting(WeaviateObject::queryMetadata)
        .returns(0.7811474, QueryMetadata::rerankScore);
  }

  /** 0.0 is a legitimate score, so absence has to come from the presence flag. */
  @Test
  public void test_rerankScoreZeroIsNotAbsent() {
    var reply = WeaviateProtoSearchGet.SearchReply.newBuilder()
        .addResults(searchResult(0.0))
        .addResults(WeaviateProtoSearchGet.SearchResult.newBuilder()
            .setMetadata(WeaviateProtoSearchGet.MetadataResult.newBuilder()
                .setId("2a5f8b3c-0f1e-4d6a-9c8b-7e2d1a0f3b4c")))
        .build();

    var objects = QueryResponse.unmarshal(reply, CollectionDescriptor.ofMap("Things")).objects();

    Assertions.assertThat(objects.get(0).queryMetadata().rerankScore()).as("reranked with 0").isEqualTo(0.0);
    Assertions.assertThat(objects.get(1).queryMetadata().rerankScore()).as("not reranked").isNull();
  }

  @Test
  public void test_groupRerankScoreIsUnmarshalled() {
    var reply = WeaviateProtoSearchGet.SearchReply.newBuilder()
        .addGroupByResults(WeaviateProtoSearchGet.GroupByResult.newBuilder()
            .setName("fish")
            .setNumberOfObjects(1)
            .addObjects(searchResult(0.7811474))
            .setRerank(WeaviateProtoSearchGet.RerankReply.newBuilder().setScore(0.42)))
        .build();

    var response = QueryResponseGrouped.unmarshal(reply,
        CollectionDescriptor.ofMap("Things"),
        CollectionHandleDefaults.of(ObjectBuilder.identity()));

    Assertions.assertThat(response.groups()).extractingByKey("fish")
        .returns(0.42, QueryResponseGroup::rerankScore);
  }

  private static WeaviateProtoSearchGet.SearchResult.Builder searchResult(double rerankScore) {
    return WeaviateProtoSearchGet.SearchResult.newBuilder()
        .setMetadata(WeaviateProtoSearchGet.MetadataResult.newBuilder()
            .setId("d3b07384-d113-4ec4-92e5-1e0f0ab84d43")
            .setRerankScore(rerankScore)
            .setRerankScorePresent(true));
  }

  private static WeaviateProtoSearchGet.SearchRequest marshal(QueryOperator operator) {
    return QueryRequest.marshal(
        new QueryRequest(operator, null),
        CollectionDescriptor.ofMap("Things"),
        CollectionHandleDefaults.of(ObjectBuilder.identity()));
  }
}
