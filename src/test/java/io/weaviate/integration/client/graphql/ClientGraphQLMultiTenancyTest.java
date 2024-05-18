package io.weaviate.integration.client.graphql;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_1;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_2;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AbstractObjectAssert;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientGraphQLMultiTenancyTest {
  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
    testGenerics = new WeaviateTestGenerics();
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }


  @Test
  public void shouldGetAllDataForTenant() {
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, TENANT_1, TENANT_2);
    testGenerics.createDataPizzaQuattroFormaggiForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaFruttiDiMareForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaHawaiiForTenants(client, TENANT_2.getName());
    testGenerics.createDataPizzaDoenerForTenants(client, TENANT_2.getName());

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
      Result<GraphQLResponse> response = client.graphQL().get()
        .withTenant(tenant)
        .withClassName("Pizza")
        .withFields(Field.builder()
          .name("_additional")
          .fields(Field.builder().name("id").build())
          .build())
        .run();

      assertGetContainsIds(response, "Pizza", expectedIds);
    });
  }

  @Test
  public void shouldGetLimitedDataForTenant() {
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, TENANT_1, TENANT_2);
    testGenerics.createDataPizzaQuattroFormaggiForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaFruttiDiMareForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaHawaiiForTenants(client, TENANT_2.getName());
    testGenerics.createDataPizzaDoenerForTenants(client, TENANT_2.getName());

    Map<String, String[]> expectedIdsByTenant = new HashMap<>();
    expectedIdsByTenant.put(TENANT_1.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
    });
    expectedIdsByTenant.put(TENANT_2.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_HAWAII_ID,
    });

    expectedIdsByTenant.forEach((tenant, expectedIds) -> {
      Result<GraphQLResponse> response = client.graphQL().get()
        .withTenant(tenant)
        .withClassName("Pizza")
        .withLimit(1)
        .withFields(Field.builder()
          .name("_additional")
          .fields(Field.builder().name("id").build())
          .build())
        .run();

      assertGetContainsIds(response, "Pizza", expectedIds);
    });
  }

  @Test
  public void shouldGetFilteredDataForTenant() {
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, TENANT_1, TENANT_2);
    testGenerics.createDataPizzaQuattroFormaggiForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaFruttiDiMareForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaHawaiiForTenants(client, TENANT_2.getName());
    testGenerics.createDataPizzaDoenerForTenants(client, TENANT_2.getName());

    Map<String, String[]> expectedIdsByTenant = new HashMap<>();
    expectedIdsByTenant.put(TENANT_1.getName(), new String[]{
      WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID,
    });
    expectedIdsByTenant.put(TENANT_2.getName(), new String[]{
    });

    expectedIdsByTenant.forEach((tenant, expectedIds) -> {
      Result<GraphQLResponse> response = client.graphQL().get()
        .withTenant(tenant)
        .withClassName("Pizza")
        .withWhere(WhereArgument.builder()
          .filter(WhereFilter.builder()
            .path(new String[]{"price"})
            .operator(Operator.GreaterThan)
            .valueNumber(2.0d)
            .build())
          .build())
        .withFields(Field.builder()
          .name("_additional")
          .fields(Field.builder().name("id").build())
          .build())
        .run();

      assertGetContainsIds(response, "Pizza", expectedIds);
    });
  }

  @Test
  public void shouldAggregateAllDataForTenant() {
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, TENANT_1, TENANT_2);
    testGenerics.createDataPizzaQuattroFormaggiForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaFruttiDiMareForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaHawaiiForTenants(client, TENANT_2.getName());
    testGenerics.createDataPizzaDoenerForTenants(client, TENANT_2.getName());

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
      Result<GraphQLResponse> response = client.graphQL().aggregate()
        .withTenant(tenant)
        .withClassName("Pizza")
        .withFields(Field.builder()
          .name("price")
          .fields(
            Field.builder().name("count").build(),
            Field.builder().name("maximum").build(),
            Field.builder().name("minimum").build(),
            Field.builder().name("median").build(),
            Field.builder().name("mean").build(),
            Field.builder().name("mode").build(),
            Field.builder().name("sum").build())
          .build())
        .run();

      assertAggregateNumFieldHasValues(response, "Pizza", "price", expectedAggValues);
    });
  }

  @Test
  public void shouldAggregateFilteredDataForTenant() {
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, TENANT_1, TENANT_2);
    testGenerics.createDataPizzaQuattroFormaggiForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaFruttiDiMareForTenants(client, TENANT_1.getName());
    testGenerics.createDataPizzaHawaiiForTenants(client, TENANT_2.getName());
    testGenerics.createDataPizzaDoenerForTenants(client, TENANT_2.getName());

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
      Result<GraphQLResponse> response = client.graphQL().aggregate()
        .withTenant(tenant)
        .withClassName("Pizza")
        .withWhere(WhereArgument.builder()
          .filter(WhereFilter.builder()
            .path(new String[]{"price"})
            .operator(Operator.GreaterThan)
            .valueNumber(2.0d)
            .build())
          .build())
        .withFields(Field.builder()
          .name("price")
          .fields(
            Field.builder().name("count").build(),
            Field.builder().name("maximum").build(),
            Field.builder().name("minimum").build(),
            Field.builder().name("median").build(),
            Field.builder().name("mean").build(),
            Field.builder().name("mode").build(),
            Field.builder().name("sum").build())
          .build())
        .run();

      assertAggregateNumFieldHasValues(response, "Pizza", "price", expectedAggValues);
    });
  }

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
      .containsExactlyInAnyOrder(expectedIds);
  }

  private void assertAggregateNumFieldHasValues(Result<GraphQLResponse> response, String className, String fieldName,
                                                Map<String, Double> expectedAggValues) {
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
}
