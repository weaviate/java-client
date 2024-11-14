package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.hc.core5.concurrent.FutureCallback;

public class SchemaDeleter implements AsyncClientResult<Boolean> {

  private final SchemaGetter schemaGetter;
  private final ClassDeleter classDeleter;

  public SchemaDeleter(SchemaGetter schemaGetter, ClassDeleter classDeleter) {
    this.schemaGetter = schemaGetter;
    this.classDeleter = classDeleter;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    CompletableFuture<Result<Schema>> getSchema = CompletableFuture.supplyAsync(() -> {
      try {
        return schemaGetter.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new CompletionException(e);
      }
    });
    CompletableFuture<Result<Boolean>> deleteAll = getSchema.thenApplyAsync(schema -> {
      if (schema.getError() != null) {
        List<WeaviateErrorMessage> errorMessages = schema.getError().getMessages().stream().map(err ->
          WeaviateErrorMessage.builder().message(err.getMessage()).build()
        ).collect(Collectors.toList());
        WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
          .error(errorMessages).build();
        return new Result<>(schema.getError().getStatusCode(), false, errors);
      } else {
        List<WeaviateClass> weaviateClasses = schema.getResult().getClasses();
        for (WeaviateClass clazz : weaviateClasses) {
          try {
            Result<Boolean> result = classDeleter.withClassName(clazz.getClassName()).run().get();
            if (result.getError() != null) {
              return result;
            }
          } catch (InterruptedException | ExecutionException e) {
            throw new CompletionException(e);
          }
        }
      }
      return new Result<>(200, true, null);
    });

    if (callback != null) {
      return deleteAll.whenComplete((booleanResult, e) -> {
        callback.completed(booleanResult);
        if (e != null) {
          callback.failed(new Exception(e));
        }
      });
    }
    return deleteAll;
  }
}
