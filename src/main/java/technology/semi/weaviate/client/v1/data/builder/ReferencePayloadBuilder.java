package technology.semi.weaviate.client.v1.data.builder;

import technology.semi.weaviate.client.v1.data.model.SingleRef;

public class ReferencePayloadBuilder {
  private String id;

  public ReferencePayloadBuilder withID(String uuid) {
    this.id = uuid;
    return this;
  }

  public SingleRef payload() {
    return SingleRef.builder()
            .beacon(String.format("weaviate://localhost/%s", this.id))
            .build();
  }
}
