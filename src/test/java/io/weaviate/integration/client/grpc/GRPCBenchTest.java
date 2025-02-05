package io.weaviate.integration.client.grpc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.integration.client.WeaviateDockerCompose;

public class GRPCBenchTest {
  @ClassRule
  public static final WeaviateDockerCompose compose = new WeaviateDockerCompose();

  private WeaviateClient client;

  private List<String> fields = new ArrayList<>();
  private final String className = "Things";

  private static final int K = 10;
  private static final Map<String, Object> filters = new HashMap<>();
  private static final Float[] query = new Float[] { .3f, .2f, .1f, -.1f, -.2f, -.3f };

  private static final List<Float[]> testData = Arrays.asList(
      new Float[] { .3f, .2f, .1f, -.1f, -.2f, -.3f },
      new Float[] { .32f, .22f, .12f, -.12f, -.22f, -.32f });

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress(), false, compose.getGrpcHostAddress());
    client = new WeaviateClient(config);

    assertTrue(write(testData), "error loading test data");
  }

  @Test
  public void testGraphQL() {
    int count = searchKNN(query, K, filters, builder -> {
      Result<GraphQLResponse> result = client
          .graphQL().raw()
          .withQuery(builder.build().buildQuery())
          .run();

      if (result.getResult() == null || result.getResult().getErrors() != null) {
        return 0;
      }
      return convertGraphQL(result);
    });

    assertTrue(count > 0, "query returned 1+ vectors");
  }

  @Test
  public void testGRPC() {
    int count = searchKNN(query, K, filters, builder -> {
      Result<List<Map<String, Object>>> result = client
          .gRPC().raw()
          .withSearch(builder.build().buildSearchRequest())
          .run();

      if (result.getResult() == null) {
        return 0;
      }
      return convertGRPC(result);
    });

    assertTrue(count > 0, "search returned 1+ vectors");
  }

  private int searchKNN(Float[] query, int k,
      Map<String, Object> filter, Function<GetBuilder.GetBuilderBuilder, Integer> search) {

    System.out.printf("search vector length: %d\n", query.length);
    NearVectorArgument nearVector = NearVectorArgument.builder().vector(query).build();

    Field[] fields = new Field[this.fields.size() + 1];
    for (int i = 0; i < this.fields.size(); i++) {
      fields[i] = Field.builder().name(this.fields.get(i)).build();
    }

    Field additional = Field.builder().name("_additional").fields(new Field[] {
        Field.builder().name("id").build(),
        Field.builder().name("vector").build(),
        Field.builder().name("distance").build()
    }).build();
    fields[this.fields.size()] = additional;

    final GetBuilder.GetBuilderBuilder builder = GetBuilder.builder()
        .className(this.className)
        .withNearVectorFilter(nearVector)
        .fields(Fields.builder().fields(fields).build())
        .limit(k);

    if (filter != null && !filter.isEmpty()) {
      WhereFilter.WhereFilterBuilder where = WhereFilter.builder();

      List<WhereFilter> operands = new ArrayList<>();
      for (String key : filter.keySet()) {
        WhereFilter wf = WhereFilter.builder().operator(Operator.Equal)
            .valueString((String) filter.get(key))
            .path(key).build();
        operands.add(wf);
      }
      where.operands(operands.toArray(new WhereFilter[operands.size()]));
      where.operator(Operator.And);
      WhereArgument arg = WhereArgument.builder().filter(where.build()).build();
      builder.withWhereFilter(arg);
    }

    return search.apply(builder);
  }

  @SuppressWarnings("unchecked")
  private int convertGraphQL(Result<GraphQLResponse> result) {
    int count = 0;
    final Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) result.getResult().getData();
    List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("Get").get(this.className);
    return list.size();

    // for (Map<String, Object> item : list) {
    // final Map<String, Object> a = (Map<String, Object>) item.get("_additional");
    // final List<Double> vector = (List<Double>) a.get("vector");
    // count++;
    // }
    // return count;
  }

  private int convertGRPC(Result<List<Map<String, Object>>> result) {
    return result.getResult().size();
  }

  public boolean write(List<Float[]> embeddings) {
    ObjectsBatcher batcher = client.batch().objectsBatcher();
    for (Float[] e : embeddings) {
      System.out.printf("insert vector length: %d\n", e.length);
      batcher.withObject(WeaviateObject.builder()
          .className(this.className)
          .vector(e)
          // .properties(meta) -> no properties, only vector
          // .id(getUuid(e)) -> use generated UUID
          .build());
    }
    final Result<ObjectGetResponse[]> run = batcher.run();
    batcher.close();

    return !run.hasErrors();
  }
}
