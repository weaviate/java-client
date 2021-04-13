package technology.semi.weaviate.client.v1.schema.api;

import java.util.List;
import java.util.stream.Collectors;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;
import technology.semi.weaviate.client.v1.schema.model.Class;
import technology.semi.weaviate.client.v1.schema.model.Schema;

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
      List<Class> classes = schema.getResult().getClasses();
      for (Class clazz : classes) {
        Result<Boolean> result = classDeleter.withClassName(clazz.getClassName()).run();
        if (result.getError() != null) {
          return result;
        }
      }
    }
    return new Result<>(200, true, null);
  }
}
