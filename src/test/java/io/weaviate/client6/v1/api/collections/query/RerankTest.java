package io.weaviate.client6.v1.api.collections.query;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
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

  private static WeaviateProtoSearchGet.SearchRequest marshal(QueryOperator operator) {
    return QueryRequest.marshal(
        new QueryRequest(operator, null),
        CollectionDescriptor.ofMap("Things"),
        CollectionHandleDefaults.of(ObjectBuilder.identity()));
  }
}
