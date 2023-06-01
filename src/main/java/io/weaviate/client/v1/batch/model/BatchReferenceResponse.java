package io.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchReferenceResponse {
  String from;
  String to;
  BatchReferenceResponseAO1Result result;
}
