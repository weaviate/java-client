package io.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class ErrorItem {
    String message;
  }
}
