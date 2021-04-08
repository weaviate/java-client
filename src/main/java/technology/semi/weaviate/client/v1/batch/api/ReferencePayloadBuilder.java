package technology.semi.weaviate.client.v1.batch.api;

import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.batch.model.BatchReference;

public class ReferencePayloadBuilder {

  private String fromClassName;
  private String fromPropertyName;
  private String fromUUID;
  private String toUUID;

  public ReferencePayloadBuilder withFromClassName(String className) {
    this.fromClassName = className;
    return this;
  }

  public ReferencePayloadBuilder withFromRefProp(String propertyName) {
    this.fromPropertyName = propertyName;
    return this;
  }

  public ReferencePayloadBuilder withFromID(String uuid) {
    this.fromUUID = uuid;
    return this;
  }

  public ReferencePayloadBuilder withToID(String uuid) {
    this.toUUID = uuid;
    return this;
  }

  public BatchReference payload() {
    if (StringUtils.isBlank(fromClassName) || StringUtils.isBlank(fromUUID) ||
            StringUtils.isBlank(fromPropertyName) || StringUtils.isBlank(toUUID)) {
      return null;
    }
    String from = String.format("weaviate://localhost/%s/%s/%s", fromClassName, fromUUID, fromPropertyName);
    String to = String.format("weaviate://localhost/%s", toUUID);
    return BatchReference.builder().from(from).to(to).build();
  }
}
