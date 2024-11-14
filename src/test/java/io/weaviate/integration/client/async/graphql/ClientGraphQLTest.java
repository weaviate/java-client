package io.weaviate.integration.client.async.graphql;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.graphql.GraphQL;
import io.weaviate.client.v1.async.graphql.api.Explore;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.async.graphql.api.Raw;
import io.weaviate.client.v1.graphql.model.ExploreFields;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;

public class ClientGraphQLTest {
    private String address;
    private String openAIApiKey;
    private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();;

    private WeaviateClient syncClient;
    private WeaviateAsyncClient client;
    private GraphQL gql;

    @ClassRule
    public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

    @Before
    public void before() {
        address = compose.getHttpHostAddress();
        openAIApiKey = System.getenv("OPENAI_APIKEY");

        syncClient = new WeaviateClient(new Config("http", address));
        testGenerics.createTestSchemaAndData(syncClient);

        client = syncClient.async();
        gql = client.graphQL();
    }

    @After
    public void after() {
        testGenerics.cleanupWeaviate(syncClient);
        client.close();
    }

    @Test
    public void testGraphQLGet() {
        Field name = Field.builder().name("name").build();

        Result<GraphQLResponse> result = doGet(get -> get.withClassName("Pizza").withFields(name));

        Map<String, List<?>> res = extractQueryResult(result, "Get");
        List<?> pizzas = extractClass(res, "Pizza");
        assertEquals(4, pizzas.size(), "wrong number of pizzas returned");
    }

    @Test
    public void testGraphQLRaw() {
        String query = "{Get{Pizza{_additional{id}}}}";

        Result<GraphQLResponse> result = doRaw(raw -> raw.withQuery(query));

        Map<String, List<?>> res = extractQueryResult(result, "Get");
        List<?> pizzas = extractClass(res, "Pizza");
        assertEquals(4, pizzas.size(), "wrong number of pizzas returned");
    }

    @Test
    public void testGraphQLExploreWithDistance() {
        ExploreFields[] fields = new ExploreFields[]{ ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME };
        String[] concepts = new String[]{ "pineapple slices", "ham" };
        NearTextMoveParameters moveTo = gql.arguments().nearTextMoveParameterBuilder().concepts(new String[]{ "Pizza" }).force(0.3f).build();
        NearTextMoveParameters moveAwayFrom = gql.arguments().nearTextMoveParameterBuilder().concepts(new String[]{ "toast", "bread" }).force(0.4f).build();
        NearTextArgument withNearText = gql.arguments().nearTextArgBuilder().concepts(concepts).distance(0.80f).moveTo(moveTo).moveAwayFrom(moveAwayFrom).build();

        Result<GraphQLResponse> result = doExplore(explore -> explore.withFields(fields).withNearText(withNearText));

        List<?> res = extractQueryResult(result, "Explore");
        assertEquals(6, res.size());
    }


    private Result<GraphQLResponse> doGet(Consumer<Get> build) {
        Get get = gql.get();
        build.accept(get);
        try {
            return get.run().get();
        } catch (InterruptedException | ExecutionException e) {
            fail("graphQL.get(): " + e.getMessage());
            return null;
        }
    }

    private Result<GraphQLResponse> doRaw(Consumer<Raw> build) {
        Raw raw = gql.raw();
        build.accept(raw);
        try {
            return raw.run().get();
        } catch (InterruptedException | ExecutionException e) {
            fail("graphQL.raw(): " + e.getMessage());
            return null;
        }
    }

    private Result<GraphQLResponse> doExplore(Consumer<Explore> build) {
        Explore explore = gql.explore();
        build.accept(explore);
        try {
            return explore.run().get();
        } catch (InterruptedException | ExecutionException e) {
            fail("graphQL.explore(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Check that request was processed successfully and no errors are returned.
     * Extract the part of the response body for the specified query type.
     * 
     * @param result    Result of a GraphQL query.
     * @param queryType "Get" or "Explore".
     * @return "data" portion of the response
     */
    @SuppressWarnings("unchecked")
    private <T> T extractQueryResult(Result<GraphQLResponse> result, String queryType) {
        assertNotNull(result, "graphQL request returned null");
        assertNull("GraphQL error in the response", result.getError());

        GraphQLResponse resp = result.getResult();
        assertNotNull(resp, "GraphQL response not returned");

        Map<String, Object> data = (Map<String, Object>) resp.getData();
        assertNotNull(data, "GraphQL response has no data");

        T queryResult = (T) data.get(queryType);
        assertNotNull(queryResult, String.format("%s query returned no result", queryType));

        return queryResult;
    }

    private <T> T extractClass(Map<String, T> queryResult, String className) {
        T objects = queryResult.get(className);
        assertNotNull(objects, String.format("no %ss returned", className.toLowerCase()));
        return objects;
    }
}