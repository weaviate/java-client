package io.weaviate.integration.client.grpc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
import io.weaviate.client.v1.experimental.Batcher;
import io.weaviate.client.v1.experimental.Collection;
import io.weaviate.client.v1.experimental.MetadataField;
import io.weaviate.client.v1.experimental.SearchResult;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.integration.client.WeaviateDockerCompose;
import lombok.AllArgsConstructor;

public class GRPCBenchTest {
  @ClassRule
  public static final WeaviateDockerCompose compose = new WeaviateDockerCompose();

  private static final Random rand = new Random();

  private WeaviateClient client;

  private String[] fields = { "description", "price", "bestBefore" };
  private final String className = "Things";

  private static final int K = 10;
  private static final Map<String, Object> filters = new HashMap<>();

  private static final int DATASET_SIZE = 10;
  private static final int VECTOR_LEN = 5000;
  private static final float VECTOR_ORIGIN = .0001f;
  private static final float VECTOR_BOUND = .001f;
  private static final List<Float[]> testData = new ArrayList<>(DATASET_SIZE);
  private static final Float[] queryVector = new Float[VECTOR_LEN];

  private static final int WARMUP_ROUNDS = 3;
  private static final int BENCHMARK_ROUNDS = 10;

  @BeforeClass
  public static void beforeAll() {
    for (int i = 0; i < DATASET_SIZE; i++) {
      testData.add(genVector(VECTOR_LEN, VECTOR_ORIGIN, VECTOR_BOUND));
    }

    // Query random vector from the dataset.
    Float[] randomVector = testData.get(rand.nextInt(0, DATASET_SIZE));
    System.arraycopy(randomVector, 0, queryVector, 0, VECTOR_LEN);

    System.out.printf("Dataset size (n. vectors): %d\n", DATASET_SIZE);
    System.out.printf("Vectors with length: %d in range %.4f-%.4f \n", VECTOR_LEN, VECTOR_ORIGIN, VECTOR_BOUND);
    System.out.println("===========================================");
  }

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress(), false, compose.getGrpcHostAddress());
    client = new WeaviateClient(config);

    assertTrue(dropSchema(), "successfully dropped schema");
    assertTrue(writeORM(testData), "loaded test data successfully");
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
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
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
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  @Test
  public void testNewClient() {
    final float[] vector = ArrayUtils.toPrimitive(queryVector);
    final Collection<Object> things = client.collections.use(className, Object.class);
    bench("GRPC.new", () -> {
      Result<List<Map<String, Object>>> result = things.query.nearVectorUntyped(
          vector,
          opt -> opt
              .limit(K)
              .returnProperties(fields)
              .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));

      int count = countGRPC(result);
      assertTrue(count > 0, "search returned 1+ vectors");
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  @AllArgsConstructor
  public static class Thing {
    public String description;
    public Double price;
    public String bestBefore;
  }

  @Test
  public void testORMClient() {
    final float[] vector = ArrayUtils.toPrimitive(queryVector);
    bench("GRPC.orm", () -> {
      Collection<Thing> things = client.collections.use(className, Thing.class);
      SearchResult<Thing> result = things.query.nearVector(
          vector,
          opt -> opt
              .limit(K)
              .returnProperties(fields)
              .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));

      int count = countORM(result);
      assertTrue(count > 0, "search returned 1+ vectors");
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  private void bench(String label, Runnable test, int warmupRounds, int benchmarkRounds) {
    long start = System.nanoTime();

    // Warmup rounds to let JVM optimise execution.
    // ---------------------------------------
    long startWarm = start;
    for (int i = 0; i < warmupRounds; i++) {
      test.run();
    }
    long finishWarm = System.nanoTime();
    double elapsedWarmNano = (finishWarm - startWarm) / 1000_000L;
    double avgWarm = elapsedWarmNano / warmupRounds;

    // Benchmarking: measure total time and divide by the number of live rounds.
    // ---------------------------------------
    long startBench = System.nanoTime();
    for (int i = 0; i < benchmarkRounds; i++) {
      test.run();
    }
    long finishBench = System.nanoTime();
    long finish = finishBench;

    double elapsedBench = (finishBench - startBench) / 1000_000L;
    double avgBench = elapsedBench / benchmarkRounds;

    double elapsed = (finish - start) / 1000_000L;

    // Print results
    // ---------------------------------------

    System.out.printf("%s\t(%d warmup, %d benchmark): \u001B[1m%.2fms\033[0m\n",
        label, warmupRounds, benchmarkRounds, avgBench);
    System.out.printf("\twarmup.round: %.2fms", avgWarm);
    System.out.printf("\t total: %.2fms\n", elapsed);
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
  }

  /* Count the number of results in the gRPC result. */
  private int countGRPC(Result<List<Map<String, Object>>> result) {
    return result.getResult().size();
  }

  /* Count the number of results in the mapped gRPC result. */
  private <T> int countORM(SearchResult<T> result) {
    return result.objects.size();
  }

  private boolean dropSchema() {
    return !client.schema().allDeleter().run().hasErrors();
  }

  private boolean write(List<Float[]> embeddings) {
    ObjectsBatcher batcher = client.batch().objectsBatcher();
    int count = 0;
    for (Float[] e : embeddings) {
      int i = count++;
      batcher.withObject(WeaviateObject.builder()
          .className(this.className)
          .vector(e)
          .properties(new HashMap<String, Object>() {
            {
              this.put("description", "Thing-" + String.valueOf(i));
              this.put("price", i);
              // FIXME(?): somehow this field is ignored if I pass Date instance here
              // and "bestBefore" cannot be requested in returnProperties.
              this.put("bestBefore", Date.from(Instant.now()).toString());
            }
          })
          // .id(getUuid(e)) -> use generated UUID
          .build());
    }
    final Result<ObjectGetResponse[]> run = batcher.run();
    batcher.close();

    return !run.hasErrors();
  }

  /** writeORM creates {@link Thing} objects and inserts them in a batch. */
  private boolean writeORM(List<Float[]> embeddings) {
    try (Batcher<Thing> batch = client.datax.batch(Thing.class)) {
      return batch.insert(b -> {
        int i = 0;
        for (Float[] e : embeddings) {
          Thing thing = new Thing(
              "Thing-" + String.valueOf(i),
              (double) i++,
              Date.from(Instant.now()).toString());
          b.add(thing, e);
        }
      });
    }
  }

  private static Float[] genVector(int length, float origin, float bound) {
    Float[] vec = new Float[length];
    for (int i = 0; i < length; i++) {
      vec[i] = rand.nextFloat(origin, bound);
    }
    return vec;
  }
}
