package io.weaviate.client.v1.batch.api;

import com.google.gson.annotations.SerializedName;
import io.weaviate.client.v1.batch.model.BatchDeleteResponse;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import lombok.Builder;
import lombok.Getter;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.filters.WhereFilter;

public class ObjectsBatchDeleter extends BaseClient<BatchDeleteResponse> implements ClientResult<BatchDeleteResponse> {

    private final ObjectsPath objectsPath;
    private String className;
    private String consistencyLevel;
    private String tenantKey;
    private WhereFilter where;
    private String output;
    private Boolean dryRun;


    public ObjectsBatchDeleter(HttpClient httpClient, Config config, ObjectsPath objectsPath) {
        super(httpClient, config);
        this.objectsPath = objectsPath;
    }


    public ObjectsBatchDeleter withClassName(String className) {
        this.className = className;
        return this;
    }

  public ObjectsBatchDeleter withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  public ObjectsBatchDeleter withTenantKey(String tenantKey) {
    this.tenantKey = tenantKey;
    return this;
  }

    public ObjectsBatchDeleter withWhere(WhereFilter where) {
        this.where = where;
        return this;
    }

    public ObjectsBatchDeleter withOutput(String output) {
        this.output = output;
        return this;
    }

    public ObjectsBatchDeleter withDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }


    @Override
    public Result<BatchDeleteResponse> run() {
        BatchDeleteMatch match = BatchDeleteMatch.builder()
                .className(className)
                .whereFilter(where)
                .build();
        BatchDelete batchDelete = BatchDelete.builder()
                .dryRun(dryRun)
                .output(output)
                .match(match)
                .build();
        String path = objectsPath.buildDelete(ObjectsPath.Params.builder()
            .consistencyLevel(consistencyLevel)
            .tenantKey(tenantKey)
            .build());
        Response<BatchDeleteResponse> resp = sendDeleteRequest(path, batchDelete, BatchDeleteResponse.class);
        return new Result<>(resp);
    }


    @Getter
    @Builder
    private static class BatchDelete {

        BatchDeleteMatch match;
        String output;
        Boolean dryRun;
    }

    @Getter
    @Builder
    private static class BatchDeleteMatch {

        @SerializedName("class")
        String className;
        @SerializedName("where")
        WhereFilter whereFilter;
    }
}
