package io.weaviate.client.v1.async.graphql.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLQuery;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearAudioArgument;
import io.weaviate.client.v1.graphql.query.argument.NearDepthArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImuArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearThermalArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.builder.AggregateBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;

public class Aggregate extends AsyncBaseClient<GraphQLResponse> implements AsyncClientResult<GraphQLResponse> {
    private final AggregateBuilder.AggregateBuilderBuilder aggregateBuilder;

    public Aggregate(CloseableHttpAsyncClient client, Config config) {
        super(client, config);
        aggregateBuilder = AggregateBuilder.builder();
    }

    public Aggregate withClassName(String className) {
        aggregateBuilder.className(className);
        return this;
    }

    public Aggregate withFields(Field... fields) {
        aggregateBuilder.fields(Fields.builder().fields(fields).build());
        return this;
    }

    @Deprecated
    public Aggregate withWhere(WhereFilter where) {
        return withWhere(WhereArgument.builder().filter(where).build());
    }

    public Aggregate withWhere(WhereArgument where) {
        aggregateBuilder.withWhereFilter(where);
        return this;
    }

    public Aggregate withGroupBy(String propertyName) {
        aggregateBuilder.groupByClausePropertyName(propertyName);
        return this;
    }

    public Aggregate withAsk(AskArgument ask) {
        aggregateBuilder.withAskArgument(ask);
        return this;
    }

    public Aggregate withNearText(NearTextArgument withNearTextFilter) {
        aggregateBuilder.withNearTextFilter(withNearTextFilter);
        return this;
    }

    public Aggregate withNearObject(NearObjectArgument withNearObjectFilter) {
        aggregateBuilder.withNearObjectFilter(withNearObjectFilter);
        return this;
    }

    public Aggregate withNearVector(NearVectorArgument withNearVectorFilter) {
        aggregateBuilder.withNearVectorFilter(withNearVectorFilter);
        return this;
    }

    public Aggregate withNearImage(NearImageArgument nearImage) {
        aggregateBuilder.withNearImageFilter(nearImage);
        return this;
    }

    public Aggregate withNearAudio(NearAudioArgument nearAudio) {
        aggregateBuilder.withNearAudioFilter(nearAudio);
        return this;
    }

    public Aggregate withNearVideo(NearVideoArgument nearVideo) {
        aggregateBuilder.withNearVideoFilter(nearVideo);
        return this;
    }

    public Aggregate withNearDepth(NearDepthArgument nearDepth) {
        aggregateBuilder.withNearDepthFilter(nearDepth);
        return this;
    }

    public Aggregate withNearThermal(NearThermalArgument nearThermal) {
        aggregateBuilder.withNearThermalFilter(nearThermal);
        return this;
    }

    public Aggregate withNearImu(NearImuArgument nearImu) {
        aggregateBuilder.withNearImuFilter(nearImu);
        return this;
    }

    public Aggregate withObjectLimit(Integer objectLimit) {
        aggregateBuilder.objectLimit(objectLimit);
        return this;
    }

    public Aggregate withTenant(String tenant) {
        aggregateBuilder.tenant(tenant);
        return this;
    }

    @Override
    public Future<Result<GraphQLResponse>> run(FutureCallback<Result<GraphQLResponse>> callback) {
        String aggregateQuery = aggregateBuilder.build().buildQuery();
        GraphQLQuery query = GraphQLQuery.builder().query(aggregateQuery).build();
        return sendPostRequest("/graphql", query, GraphQLResponse.class, callback);
    }

}
