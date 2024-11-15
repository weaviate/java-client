package io.weaviate.integration.client.async.graphql;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.graphql.GraphQL;
import io.weaviate.client.v1.async.graphql.api.Aggregate;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_1;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ClientGraphQLMultiTenancyTest extends AbstractAsyncClientTest {
  private static final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  private String address;

  private WeaviateClient syncClient;
  private WeaviateAsyncClient client;
  private GraphQL gql;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    address = compose.getHttpHostAddress();

    syncClient = new WeaviateClient(new Config("http", address));

    testGenerics.createSchemaPizzaForTenants(syncClient);
    testGenerics.createTenantsPizza(syncClient, TENANT_1, TENANT_2);
    testGenerics.createDataPizzaQuattroFormaggiForTenants(syncClient, TENANT_1.getName());
    testGenerics.createDataPizzaFruttiDiMareForTenants(syncClient, TENANT_1.getName());
    testGenerics.createDataPizzaHawaiiForTenants(syncClient, TENANT_2.getName());
    testGenerics.createDataPizzaDoenerForTenants(syncClient, TENANT_2.getName());

    client = syncClient.async();
    gql = client.graphQL();
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(syncClient);
    client.close();
  }

  @Test
  public void shouldGetAllDataForTenant() {
    Map<String, String[]> expectedIdsByTenant = new HashMap<>();
    expectedIdsByTenant.put(TENANT_1.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
      WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID,
    });
    expectedIdsByTenant.put(TENANT_2.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_HAWAII_ID,
      WeaviateTestGenerics.PIZZA_DOENER_ID,
    });

    expectedIdsByTenant.forEach((tenant, expectedIds) -> {
      Result<GraphQLResponse> response = doGet(get -> get
        .withTenant(tenant)
        .withClassName("Pizza")
        .withFields(_additional("id")));

      assertGetContainsIds(response, "Pizza", expectedIds);
    });
  }

  @Test
  public void shouldGetLimitedDataForTenant() {
    Map<String, String[]> expectedIdsByTenant = new HashMap<>();
    expectedIdsByTenant.put(TENANT_1.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
    });
    expectedIdsByTenant.put(TENANT_2.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_HAWAII_ID,
    });

    expectedIdsByTenant.forEach((tenant, expectedIds) -> {
      Result<GraphQLResponse> response = doGet(get -> get
        .withTenant(tenant)
        .withClassName("Pizza")
        .withLimit(1)
        .withFields(_additional("id")));

      assertGetContainsIds(response, "Pizza", expectedIds);
    });
  }

  @Test
  public void shouldGetFilteredDataForTenant() {
    Map<String, String[]> expectedIdsByTenant = new HashMap<>();
    expectedIdsByTenant.put(TENANT_1.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID,
    });
    expectedIdsByTenant.put(TENANT_2.getName(), new String[]{
    });

    expectedIdsByTenant.forEach((tenant, expectedIds) -> {
      Result<GraphQLResponse> response = doGet(get -> get
        .withTenant(tenant)
        .withClassName("Pizza")
        .withWhere(whereNumber("price", Operator.GreaterThan, 2.0d))
        .withFields(_additional("id")));

      assertGetContainsIds(response, "Pizza", expectedIds);
    });
  }

  @Test
  public void shouldAggregateAllDataForTenant() {
    Map<String, Map<String, Double>> expectedAggValuesByTenant = new HashMap<>();
    expectedAggValuesByTenant.put(TENANT_1.getName(), new HashMap<String, Double>() {{
      put("count", 2.0);
      put("maximum", 2.5);
      put("minimum", 1.4);
      put("median", 1.95);
      put("mean", 1.95);
      put("mode", 1.4);
      put("sum", 3.9);
    }});
    expectedAggValuesByTenant.put(TENANT_2.getName(), new HashMap<String, Double>() {{
      put("count", 2.0);
      put("maximum", 1.2);
      put("minimum", 1.1);
      put("median", 1.15);
      put("mean", 1.15);
      put("mode", 1.1);
      put("sum", 2.3);
    }});

    expectedAggValuesByTenant.forEach((tenant, expectedAggValues) -> {
      Result<GraphQLResponse> response = doAggregate(aggregate -> aggregate
        .withTenant(tenant)
        .withClassName("Pizza")
        .withFields(Field.builder()
          .name("price")
          .fields(fields(
            "count",
            "maximum",
            "minimum",
            "median",
            "mean",
            "mode",
            "sum"
          )).build()));

      assertAggregateNumFieldHasValues(response, "Pizza", "price", expectedAggValues);
    });
  }

  @Test
  public void shouldAggregateFilteredDataForTenant() {
    Map<String, Map<String, Double>> expectedAggValuesByTenant = new HashMap<>();
    expectedAggValuesByTenant.put(TENANT_1.getName(), new HashMap<String, Double>() {{
      put("count", 1.0);
      put("maximum", 2.5);
      put("minimum", 2.5);
      put("median", 2.5);
      put("mean", 2.5);
      put("mode", 2.5);
      put("sum", 2.5);
    }});
    expectedAggValuesByTenant.put(TENANT_2.getName(), new HashMap<String, Double>() {{
      put("count", 0.0);
      put("maximum", null);
      put("minimum", null);
      put("median", null);
      put("mean", null);
      put("mode", null);
      put("sum", null);
    }});

    expectedAggValuesByTenant.forEach((tenant, expectedAggValues) -> {
      Result<GraphQLResponse> response = doAggregate(aggregate -> aggregate
        .withTenant(tenant)
        .withClassName("Pizza")
        .withWhere(whereNumber("price", Operator.GreaterThan, 2.0d))
        .withFields(Field.builder()
          .name("price")
          .fields(fields(
            "count",
            "maximum",
            "minimum",
            "median",
            "mean",
            "mode",
            "sum"
          )).build()));

      assertAggregateNumFieldHasValues(response, "Pizza", "price", expectedAggValues);
    });
  }

  @SuppressWarnings("unchecked")
  private void assertGetContainsIds(Result<GraphQLResponse> response, String className, String... expectedIds) {
    assertThat(response).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
      .extracting(data -> ((Map<String, Object>) data).get("Get")).isInstanceOf(Map.class)
      .extracting(get -> ((Map<String, Object>) get).get(className)).isInstanceOf(List.class).asList()
      .hasSize(expectedIds.length)
      .extracting(obj -> ((Map<String, Object>) obj).get("_additional"))
      .extracting(add -> ((Map<String, Object>) add).get("id"))
      .containsExactlyInAnyOrder((Object[]) expectedIds);
  }

  @SuppressWarnings("unchecked")
  private void assertAggregateNumFieldHasValues(
    Result<GraphQLResponse> response, String className, String fieldName,
    Map<String, Double> expectedAggValues
  ) {
    AbstractObjectAssert<?, Object> aggregate = assertThat(response).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
      .extracting(data -> ((Map<String, Object>) data).get("Aggregate")).isInstanceOf(Map.class)
      .extracting(agg -> ((Map<String, Object>) agg).get(className)).isInstanceOf(List.class).asList()
      .hasSize(1)
      .first()
      .extracting(obj -> ((Map<String, Object>) obj).get(fieldName)).isInstanceOf(Map.class);

    expectedAggValues.forEach((name, value) -> aggregate.returns(value, map -> ((Map<String, Double>) map).get(name)));
  }

  private Result<GraphQLResponse> doGet(Consumer<Get> build) {
    Get get = gql.get();
    build.accept(get);
    try {
      return get.run()
        .get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.get(): " + e.getMessage());
      return null;
    }
  }

  private Result<GraphQLResponse> doAggregate(Consumer<Aggregate> build) {
    Aggregate aggregate = gql.aggregate();
    build.accept(aggregate);
    try {
      return aggregate.run()
        .get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.aggregate(): " + e.getMessage());
      return null;
    }
  }
}
