package io.weaviate.client.v1.schema.api;

import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.List;
import java.util.stream.Collectors;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;

public class SchemaDeleter {
  private SchemaGetter schemaGetter;
  private ClassDeleter classDeleter;

  public SchemaDeleter(SchemaGetter schemaGetter, ClassDeleter classDeleter) {
    this.schemaGetter = schemaGetter;
    this.classDeleter = classDeleter;
  }

  public Result<Boolean> run() {
    Result<Schema> schema = schemaGetter.run();
    if (schema.getError() != null) {
      List<WeaviateErrorMessage> errorMessages = schema.getError().getMessages().stream().map(err ->
              WeaviateErrorMessage.builder().message(err.getMessage()).build()
      ).collect(Collectors.toList());
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(errorMessages).build();
      return new Result<>(schema.getError().getStatusCode(), false, errors);
    }
    if (schema.getError() == null) {
      List<WeaviateClass> weaviateClasses = schema.getResult().getClasses();
      for (WeaviateClass clazz : weaviateClasses) {
        Result<Boolean> result = classDeleter.withClassName(clazz.getClassName()).run();
        if (result.getError() != null) {
          return result;
        }
      }
    }
    return new Result<>(200, true, null);
  }
}
