package io.weaviate.integration.client.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
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
import io.weaviate.client.v1.experimental.Operand;
import io.weaviate.client.v1.experimental.SearchResult;
import io.weaviate.client.v1.experimental.Where;
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
import lombok.ToString;

public class GRPCBenchTest {
  @ClassRule
  public static final WeaviateDockerCompose compose = new WeaviateDockerCompose();

  private static final Random rand = new Random();

  private WeaviateClient client;

  private static final String[] returnProperties = { "title", "price", "bestBefore", "possiblyNull" };
  private static final String className = "Things";
  private static final Date NOW = Date.from(Instant.now());

  private static final int K = 10;
  private static final String[] notIngredients = { "ketchup", "mayo" };
  private static final Map<String, Object> notEqualFilters = new HashMap<String, Object>() {
    {
      this.put("title", "SomeThing");
      this.put("price", 8);
      this.put("bestBefore", DateUtils.addDays(NOW, 5));
    }
  };
  private static final Map<String, Object> arrayListFilters = new HashMap<String, Object>() {
    {
      this.put("ingredientsList", Arrays.asList(notIngredients));
      this.put("ingredientsArray", notIngredients);
    }
  };

  private static final int DATASET_SIZE = 30;
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
    int randomIdx = Math.abs(rand.nextInt()) % DATASET_SIZE;
    Float[] randomVector = testData.get(randomIdx);
    System.arraycopy(randomVector, 0, queryVector, 0, VECTOR_LEN);

    System.out.printf("Dataset size (n. vectors): %d\n", DATASET_SIZE);
    System.out.printf("Vectors with length: %d in range %.4f-%.4f\n", VECTOR_LEN, VECTOR_ORIGIN, VECTOR_BOUND);
    System.out.printf("Search vector #%d\n", randomIdx);
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
      int count = searchKNN(queryVector, K, notEqualFilters, builder -> {
        Result<GraphQLResponse> result = client
            .graphQL().raw()
            .withQuery(builder.build().buildQuery())
            .run();

        if (result.getResult() == null || result.getResult().getErrors() != null) {
          return 0;
        }
        return convertGraphQL(result);
      });

      assertEquals(K, count, String.format("must return K=%d results", K));
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  @Test
  public void testGRPC() {
    bench("GRPC", () -> {
      int count = searchKNN(queryVector, K, notEqualFilters, builder -> {
        SearchResult<Map<String, Object>> result = client
            .gRPC().raw()
            .withSearch(builder.build().buildSearchRequest())
            .run();

        return countGRPC(result);
      });

      assertEquals(K, count, String.format("must return K=%d results", K));
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  @Test
  public void testNewClient() {
    final float[] vector = ArrayUtils.toPrimitive(queryVector);
    final Collection<Map> things = client.collections.use(className, Map.class);
    bench("GRPC.new", () -> {
      SearchResult<Map<String, Object>> result = things.query.nearVectorUntyped(
          vector,
          opt -> opt
              .limit(K)
              .returnProperties(returnProperties) // Optional: skip this field to retrieve ALL properties
              .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));

      int count = countGRPC(result);
      assertEquals(K, count, String.format("must return K=%d results", K));
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  @AllArgsConstructor
  @ToString
  public static class Thing {
    public String title;
    public Double price;
    public Date bestBefore;

    public String[] ingredientsArray = {};
    // WARN: this is to test filtering with List<?> values. Creating List<?>
    // properties is not supported in this version.
    public String[] ingredientsList = {};

    // Property containing null values.
    public String possiblyNull;
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
              .returnProperties(returnProperties)
              .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));

      int count = countGRPC(result);
      assertEquals(K, count, String.format("must return K=%d results", K));

      Assertions.assertThat(result.objects).allSatisfy(
          object -> {
            Assertions.assertThat(object.metadata.id)
                .isNotNull().as("must retrieve id");
            Assertions.assertThat(object.metadata.vector)
                .isNotNull().as("must retrieve vector")
                .hasSize(VECTOR_LEN).as("vector has expected size");
          });

    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  @Test
  public void testORMClientMapFilter() {
    final float[] vector = ArrayUtils.toPrimitive(queryVector);
    bench("GRPC.map-filter", () -> {
      Collection<Thing> things = client.collections.use(className, Thing.class);

      SearchResult<Thing> result = things.query.nearVector(
          vector,
          opt -> opt
              .limit(K)
              .where(Where.or(
                  // Constructed from a Map<String, Object>!
                  Where.and(notEqualFilters, Where.Operator.NOT_EQUAL),
                  Where.and(arrayListFilters, Where.Operator.CONTAINS_ALL)))
              .returnProperties(returnProperties)
              .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));

      int count = countGRPC(result);
      assertEquals(K, count, String.format("must return K=%d results", K));

      // Check that filtering works
      assertFalse(result.objects.stream().anyMatch(obj -> obj.properties.title.equals(notEqualFilters.get("title"))),
          "expected title to not be in result set: " + notEqualFilters.get("title"));

      assertFalse(result.objects.stream().anyMatch(obj -> obj.properties.price.equals(notEqualFilters.get("price"))),
          "expected price to not be in result set: " + notEqualFilters.get("price"));
    }, WARMUP_ROUNDS, BENCHMARK_ROUNDS);
  }

  public void exampleORMWithHardcodedFilters() {
    final float[] vector = ArrayUtils.toPrimitive(queryVector);
    Operand[] whereFilters = {
        Where.property("title").eq("Thing A"),
        Where.property("price").gte(1.94f),
        Where.or(
            Where.property("bestBefore").lte(Date.from(Instant.now())),
            Where.property("bestBefore").ne(Date.from(Instant.now().plusSeconds(20)))),
    };

    Collection<Thing> things = client.collections.use(className, Thing.class);
    things.query.nearVector(
        vector,
        opt -> opt
            .limit(K)
            .where(Where.and(whereFilters))
            // .where(Where.and()) -> ignored, because no filters are applied
            .returnProperties(returnProperties)
            .returnMetadata(MetadataField.ID, MetadataField.VECTOR, MetadataField.DISTANCE));
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

    Field[] fields = new Field[returnProperties.length + 1];
    for (int i = 0; i < returnProperties.length; i++) {
      fields[i] = Field.builder().name(returnProperties[i]).build();
    }

    Field additional = Field.builder().name("_additional").fields(new Field[] {
        Field.builder().name("id").build(),
        Field.builder().name("vector").build(),
        Field.builder().name("distance").build()
    }).build();
    fields[returnProperties.length] = additional;

    final GetBuilder.GetBuilderBuilder builder = GetBuilder.builder()
        .className(className)
        .withNearVectorFilter(nearVector)
        .fields(Fields.builder().fields(fields).build())
        .limit(k);

    if (filter != null && !filter.isEmpty()) {
      WhereFilter.WhereFilterBuilder where = WhereFilter.builder();

      List<WhereFilter> operands = new ArrayList<>();
      for (String key : filter.keySet()) {
        Object filterValue = filter.get(key);
        if (!(filterValue instanceof String)) {
          continue; // This method only supports filtering on strings.
        }
        WhereFilter wf = WhereFilter.builder().operator(Operator.NotEqual)
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
    List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("Get").get(className);
    return list.size();
  }

  /* Count the number of results in the gRPC result. */
  private int countGRPC(Result<List<Map<String, Object>>> result) {
    return result.getResult().size();
  }

  /* Count the number of results in the mapped gRPC result. */
  private <T> int countGRPC(SearchResult<T> result) {
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
      String[] ingr = mixIngredients();
      batcher.withObject(WeaviateObject.builder()
          .className(className)
          .vector(e)
          .properties(new HashMap<String, Object>() {
            {
              this.put("title", "Thing-" + String.valueOf(i));
              this.put("price", i);
              this.put("bestBefore", DateFormatUtils.format(DateUtils.addDays(NOW, i), "yyyy-MM-dd'T'HH:mm:ssZZZZZ"));
              this.put("ingredientsArray", ingr);
              this.put("ingredientsList", ingr);
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
      String[] ingr = mixIngredients();
      return batch.insert(b -> {
        int i = 0;
        for (Float[] e : embeddings) {
          Thing thing = new Thing(
              /* title */ "Thing-" + String.valueOf(i),
              /* price */ (double) i,

              // Notice how the ORM is able to handle a raw Date object
              // and convert it to the correct format behind the scenes.
              /* bestBefore */ DateUtils.addDays(NOW, i),
              /* ingredientsArray */ ingr,
              /* ingredientsList */ ingr,
              i == 2 ? "not null" : null);
          b.add(thing, e);
          i++;
        }
      });
    }
  }

  /** Utility for creating random combinations of ingredients for test data. */
  private String[] mixIngredients() {
    return Arrays.stream(new String[] { "milk", "honey", "butter" })
        .filter(x -> rand.nextBoolean()).toArray(String[]::new);
  }

  private static Float[] genVector(int length, float origin, float bound) {
    Float[] vec = new Float[length];
    for (int i = 0; i < length; i++) {
      vec[i] = (Math.abs(rand.nextFloat()) % (bound - origin + 1)) + origin;
    }
    return vec;
  }
}
