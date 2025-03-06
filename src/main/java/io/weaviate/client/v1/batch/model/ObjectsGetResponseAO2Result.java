package io.weaviate.client.v1.batch.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectsGetResponseAO2Result {
  ErrorResponse errors;
  String status;

  @Getter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class ErrorResponse {
    List<ErrorItem> error;

    public ErrorResponse(String... errors) {
      this.error = Arrays.stream(errors).map(ErrorItem::new).collect(Collectors.toList());
    }
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @AllArgsConstructor
  public static class ErrorItem {
    String message;
  }
}
