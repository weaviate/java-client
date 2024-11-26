package io.weaviate.integration.tests.graphql;

import com.google.gson.annotations.SerializedName;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLGetBaseObject;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClientGraphQLTypedTestSuite {

  @Getter
  public static class Pizzas {
    @SerializedName(value = "Pizza")
    List<Pizza> pizzas;

    @Getter
    public static class Pizza extends GraphQLGetBaseObject {
      String name;
      String description;
      String bestBefore;
      Float price;
    }
  }

  public static void testGraphQLGet(Supplier<Result<GraphQLTypedResponse<Pizzas>>> supplyPizza) {
    // given
    Result<GraphQLTypedResponse<Pizzas>> result = supplyPizza.get();
    // then
    assertNotNull(result);
    assertFalse(result.hasErrors());
    GraphQLTypedResponse<ClientGraphQLTypedTestSuite.Pizzas> gqlResult = result.getResult();
    assertNotNull(gqlResult);
    assertNotNull(gqlResult.getData());
    GraphQLTypedResponse.Operation<ClientGraphQLTypedTestSuite.Pizzas> resp = gqlResult.getData();
    assertNotNull(resp.getObjects());
    assertNotNull(resp.getObjects().getPizzas());
    List<ClientGraphQLTypedTestSuite.Pizzas.Pizza> pizzas = resp.getObjects().getPizzas();
    assertTrue(pizzas.size() == 4);
    String name = pizzas.get(0).getName();
    assertNotNull(name);
    assertTrue(name.length() > 0);
    String description = pizzas.get(0).getDescription();
    assertNotNull(description);
    assertTrue(description.length() > 0);
    assertNull(pizzas.get(0).getPrice());
    assertNull(pizzas.get(0).getBestBefore());
  }
}
