package technology.semi.weaviate.client.base;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WeaviateError {
  int statusCode;
  List<WeaviateErrorMessage> messages;
}
