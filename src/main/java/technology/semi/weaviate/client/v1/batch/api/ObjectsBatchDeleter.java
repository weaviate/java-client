package technology.semi.weaviate.client.v1.batch.api;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.model.BatchDeleteResponse;
import technology.semi.weaviate.client.v1.filters.WhereFilter;

public class ObjectsBatchDeleter extends BaseClient<BatchDeleteResponse> implements ClientResult<BatchDeleteResponse> {

    private String className;
    private WhereFilter where;
    private String output;
    private Boolean dryRun;


    public ObjectsBatchDeleter(Config config) {
        super(config);
    }


    public ObjectsBatchDeleter withClassName(String className) {
        this.className = className;
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
        Response<BatchDeleteResponse> resp = sendDeleteRequest("/batch/objects", batchDelete, BatchDeleteResponse.class);
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
