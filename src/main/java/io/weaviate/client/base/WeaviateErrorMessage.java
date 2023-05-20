package io.weaviate.client.base;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WeaviateErrorMessage {
  String message;
  transient Throwable throwable; // transient = not serialized by gson. This field is only used on Java.
}
