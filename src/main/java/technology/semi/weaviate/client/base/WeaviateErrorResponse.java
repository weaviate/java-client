package technology.semi.weaviate.client.base;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WeaviateErrorResponse {
  Integer code;
  String message;
  List<WeaviateErrorMessage> error;
}
