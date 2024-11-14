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
import io.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import io.weaviate.client.v1.graphql.query.argument.GroupArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupByArgument;
import io.weaviate.client.v1.graphql.query.argument.HybridArgument;
import io.weaviate.client.v1.graphql.query.argument.NearAudioArgument;
import io.weaviate.client.v1.graphql.query.argument.NearDepthArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImuArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearThermalArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArguments;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.graphql.query.fields.GenerativeSearchBuilder;

public class Get extends AsyncBaseClient<GraphQLResponse> implements AsyncClientResult<GraphQLResponse> {
    private final GetBuilder.GetBuilderBuilder getBuilder;

    public Get(CloseableHttpAsyncClient client, Config config) {
        super(client, config);
        getBuilder = GetBuilder.builder();
    }

    public Get withClassName(String className) {
        getBuilder.className(className);
        return this;
    }

    public Get withFields(Field... fields) {
        getBuilder.fields(Fields.builder().fields(fields).build());
        return this;
    }

    @Deprecated
    public Get withWhere(WhereFilter where) {
        return withWhere(WhereArgument.builder().filter(where).build());
    }

    public Get withWhere(WhereArgument where) {
        getBuilder.withWhereFilter(where);
        return this;
    }

    public Get withLimit(Integer limit) {
        getBuilder.limit(limit);
        return this;
    }

    public Get withOffset(Integer offset) {
        getBuilder.offset(offset);
        return this;
    }

    public Get withAfter(String after) {
        getBuilder.after(after);
        return this;
    }

    public Get withBm25(Bm25Argument bm25) {
        getBuilder.withBm25Filter(bm25);
        return this;
    }

    public Get withHybrid(HybridArgument hybrid) {
        getBuilder.withHybridFilter(hybrid);
        return this;
    }

    public Get withAsk(AskArgument ask) {
        getBuilder.withAskArgument(ask);
        return this;
    }

    public Get withNearText(NearTextArgument nearText) {
        getBuilder.withNearTextFilter(nearText);
        return this;
    }

    public Get withNearObject(NearObjectArgument nearObject) {
        getBuilder.withNearObjectFilter(nearObject);
        return this;
    }

    public Get withNearVector(NearVectorArgument nearVector) {
        getBuilder.withNearVectorFilter(nearVector);
        return this;
    }

    public Get withNearImage(NearImageArgument nearImage) {
        getBuilder.withNearImageFilter(nearImage);
        return this;
    }

    public Get withNearAudio(NearAudioArgument nearAudio) {
        getBuilder.withNearAudioFilter(nearAudio);
        return this;
    }

    public Get withNearVideo(NearVideoArgument nearVideo) {
        getBuilder.withNearVideoFilter(nearVideo);
        return this;
    }

    public Get withNearDepth(NearDepthArgument nearDepth) {
        getBuilder.withNearDepthFilter(nearDepth);
        return this;
    }

    public Get withNearThermal(NearThermalArgument nearThermal) {
        getBuilder.withNearThermalFilter(nearThermal);
        return this;
    }

    public Get withNearImu(NearImuArgument nearImu) {
        getBuilder.withNearImuFilter(nearImu);
        return this;
    }

    public Get withGroup(GroupArgument group) {
        getBuilder.withGroupArgument(group);
        return this;
    }

    public Get withSort(SortArgument... sort) {
        getBuilder.withSortArguments(SortArguments.builder().sort(sort).build());
        return this;
    }

    public Get withGenerativeSearch(GenerativeSearchBuilder generativeSearch) {
        getBuilder.withGenerativeSearch(generativeSearch);
        return this;
    }

    public Get withConsistencyLevel(String level) {
        getBuilder.withConsistencyLevel(level);
        return this;
    }

    public Get withGroupBy(GroupByArgument groupBy) {
        getBuilder.withGroupByArgument(groupBy);
        return this;
    }

    public Get withTenant(String tenant) {
        getBuilder.tenant(tenant);
        return this;
    }

    public Get withAutocut(Integer autocut) {
        getBuilder.autocut(autocut);
        return this;
    }

    @Override
    public Future<Result<GraphQLResponse>> run(FutureCallback<Result<GraphQLResponse>> callback) {
        String getQuery = getBuilder.build().buildQuery();
        GraphQLQuery query = GraphQLQuery.builder().query(getQuery).build();
        return sendPostRequest("/graphql", query, GraphQLResponse.class, callback);
    }
}
