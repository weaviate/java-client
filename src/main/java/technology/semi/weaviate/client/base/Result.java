package technology.semi.weaviate.client.base;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Result<T> {
  T result;
  WeaviateError error;

  public Result(Response<T> response) {
    this(response.getStatusCode(), response.getBody(), response.getErrors());
  }

  public Result(int statusCode, T body, WeaviateErrorResponse errors) {
    if (errors != null && errors.getError() != null) {
      List<WeaviateErrorMessage> items = errors.getError().stream().filter(Objects::nonNull).collect(Collectors.toList());
      this.error = new WeaviateError(statusCode, items);
      this.result = null;
    } else {
      this.result = body;
      this.error = null;
    }
  }
}
