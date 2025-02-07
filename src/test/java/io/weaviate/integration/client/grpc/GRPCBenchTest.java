package io.weaviate.integration.client.grpc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.experimental.Collection;
import io.weaviate.client.v1.experimental.MetadataField;
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

  private static final Random rand = new Random();

  private WeaviateClient client;

  private String[] fields = {};
  private final String className = "Things";

  private static final int K = 10;
  private static final Map<String, Object> filters = new HashMap<>();

  private static final int datasetSize = 10;
  private static final int vectorLength = 5000;
  private static final float vectorOrigin = .0001f;
  private static final float vectorBound = .001f;
  private static final List<Float[]> testData = new ArrayList<>(datasetSize);
  private static final Float[] queryVector = new Float[vectorLength];

  @BeforeClass
  public static void beforeAll() {
    for (int i = 0; i < datasetSize; i++) {
      testData.add(genVector(vectorLength, vectorOrigin, vectorBound));
    }

    // Query random vector from the dataset.
    Float[] randomVector = testData.get(rand.nextInt(0, datasetSize));
    System.arraycopy(randomVector, 0, queryVector, 0, vectorLength);

    System.out.printf("Dataset size (n. vectors): %d\n", datasetSize);
    System.out.printf("Vectors with length: %d in range %.4f-%.4f \n", vectorLength, vectorOrigin, vectorBound);
    System.out.println("===========================================");
  }

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress(), false, compose.getGrpcHostAddress());
    client = new WeaviateClient(config);

    assertTrue(dropSchema(), "successfully dropped schema");
    assertTrue(write(testData), "loaded test data successfully");
  }

  @Test
  public void testGraphQL() {
    bench("GraphQL", () -> {
      int count = searchKNN(queryVector, K, filters, builder -> {
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
    }, 3, 10);
  }

  @Test
  public void testGRPC() {
    bench("GRPC", () -> {
      int count = searchKNN(queryVector, K, filters, builder -> {
        Result<List<Map<String, Object>>> result = client
            .gRPC().raw()
            .withSearch(builder.build().buildSearchRequest())
            .run();

        if (result.getResult() == null) {
          return 0;
        }
        return countGRPC(result);
      });

      assertTrue(count > 0, "search returned 1+ vectors");
    }, 3, 10);
  }

  @Test
  public void testNewClient() {
    final float[] vector = ArrayUtils.toPrimitive(queryVector);
    bench("GRPC.new", () -> {
      Collection things = client.collections.use(this.className);
      Result<List<Map<String, Object>>> result = things.query.nearVector(
          vector,
          opt -> opt
              .limit(K)
              .returnProperties(this.fields)
              .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));

      int count = countGRPC(result);
      assertTrue(count > 0, "search returned 1+ vectors");
    }, 3, 10);
  }

  private void bench(String label, Runnable test, int warmupRounds, int benchmarkRounds) {
    Instant start = Instant.now();

    // Warmup rounds to let JVM optimise execution.
    // ---------------------------------------
    Instant startWarm = start;
    for (int i = 0; i < warmupRounds; i++) {
      test.run();
    }
    Instant finishWarm = Instant.now();
    long elapsedWarm = Duration.between(startWarm, finishWarm).toMillis();
    float avgWarm = elapsedWarm / warmupRounds;

    // Benchmarking: measure total time and divide by the number of live rounds.
    // ---------------------------------------
    Instant startBench = Instant.now();
    for (int i = 0; i < benchmarkRounds; i++) {
      test.run();
    }
    Instant finishBench = Instant.now();
    Instant finish = finishBench;

    long elapsedBench = Duration.between(startBench, finishBench).toMillis();
    float avgBench = elapsedBench / benchmarkRounds;

    long elapsed = Duration.between(start, finish).toMillis();

    // Print results
    // ---------------------------------------

    System.out.printf("%s\t(%d warmup, %d benchmark): \u001B[1m%.1fms\033[0m\n", label, warmupRounds, benchmarkRounds,
        avgBench);
    System.out.printf("\twarmup.round: %.1fms", avgWarm);
    System.out.printf("\t total: %dms\n", elapsed);
  }

  private int searchKNN(Float[] query, int k,
      Map<String, Object> filter, Function<GetBuilder.GetBuilderBuilder, Integer> search) {

    NearVectorArgument nearVector = NearVectorArgument.builder().vector(query).build();

    Field[] fields = new Field[this.fields.length + 1];
    for (int i = 0; i < this.fields.length; i++) {
      fields[i] = Field.builder().name(this.fields[i]).build();
    }

    Field additional = Field.builder().name("_additional").fields(new Field[] {
        Field.builder().name("id").build(),
        Field.builder().name("vector").build(),
        Field.builder().name("distance").build()
    }).build();
    fields[this.fields.length] = additional;

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

  /* Count the number of results in the GraphQL result. */
  @SuppressWarnings("unchecked")
  private int convertGraphQL(Result<GraphQLResponse> result) {
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

  /* Count the number of results in the gRPC result. */
  private int countGRPC(Result<List<Map<String, Object>>> result) {
    return result.getResult().size();
  }

  private boolean dropSchema() {
    return !client.schema().allDeleter().run().hasErrors();
  }

  private boolean write(List<Float[]> embeddings) {
    ObjectsBatcher batcher = client.batch().objectsBatcher();
    for (Float[] e : embeddings) {
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

  private static Float[] genVector(int length, float origin, float bound) {
    Float[] vec = new Float[length];
    for (int i = 0; i < length; i++) {
      vec[i] = rand.nextFloat(origin, bound);
    }
    return vec;
  }
}
