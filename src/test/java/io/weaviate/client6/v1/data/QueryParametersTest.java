package io.weaviate.client6.v1.data;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

@RunWith(JParamsTestRunner.class)
public class QueryParametersTest {

  public static Object[][] testCases() {
    return new Object[][] {
        {
            QueryParameters.encodeGet(q -> q
                .withVector()
                .nodeName("node-1")),
            "?include=vector&node_name=node-1",
        },
        {
            QueryParameters.encodeGet(q -> q
                .withVector()
                .withClassification()
                .tenant("JohnDoe")),
            "?include=vector,classification&tenant=JohnDoe",
        },
        {
            QueryParameters.encodeGet(q -> q
                .consistencyLevel(ConsistencyLevel.ALL)
                .nodeName("node-1")
                .tenant("JohnDoe")),
            "?consistency_level=ALL&node_name=node-1&tenant=JohnDoe",
        },
        {
            QueryParameters.encodeGet(q -> {
            }),
            "",
        },
    };
  }

  @Test
  @DataMethod(source = QueryParametersTest.class, method = "testCases")
  public void testEncode(String got, String want) {
    Assertions.assertThat(got).isEqualTo(want).as("expected query parameters");
  }
}
