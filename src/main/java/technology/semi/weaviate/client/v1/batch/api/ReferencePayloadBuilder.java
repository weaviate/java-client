package technology.semi.weaviate.client.v1.batch.api;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.v1.batch.model.BatchReference;

public class ReferencePayloadBuilder {

  private final BeaconPath beaconPath;
  private String fromUUID;
  private String fromClassName;
  private String fromPropertyName;
  private String toUUID;
  private String toClassName;

  @Deprecated
  public ReferencePayloadBuilder() {
    this.beaconPath = null;
    System.err.println("WARNING: Deprecated constructor for ReferencePayloadBuilder class was used. Please use parametrized one.");
  }

  public ReferencePayloadBuilder(BeaconPath beaconPath) {
    this.beaconPath = Objects.requireNonNull(beaconPath);
  }

  public ReferencePayloadBuilder withFromID(String uuid) {
    this.fromUUID = uuid;
    return this;
  }

  public ReferencePayloadBuilder withFromClassName(String className) {
    this.fromClassName = className;
    return this;
  }

  public ReferencePayloadBuilder withFromRefProp(String propertyName) {
    this.fromPropertyName = propertyName;
    return this;
  }

  public ReferencePayloadBuilder withToID(String uuid) {
    this.toUUID = uuid;
    return this;
  }

  public ReferencePayloadBuilder withToClassName(String className) {
    this.toClassName = className;
    return this;
  }

  public BatchReference payload() {
    if (StringUtils.isBlank(fromClassName) || StringUtils.isBlank(fromUUID) ||
      StringUtils.isBlank(fromPropertyName) || StringUtils.isBlank(toUUID)) {
      return null;
    }

    String from;
    String to;
    if (beaconPath != null) {
      from = beaconPath.buildBatchFrom(BeaconPath.Params.builder()
        .id(fromUUID)
        .className(fromClassName)
        .property(fromPropertyName)
        .build());
      to = beaconPath.buildBatchTo(BeaconPath.Params.builder()
        .id(toUUID)
        .className(toClassName)
        .build());
    } else {
      from = beaconFromDeprecated();
      to = beaconToDeprecated();
    }

    return BatchReference.builder().from(from).to(to).build();
  }

  private String beaconFromDeprecated() {
    return String.format("weaviate://localhost/%s/%s/%s", fromClassName, fromUUID, fromPropertyName);
  }

  private String beaconToDeprecated() {
    return String.format("weaviate://localhost/%s", toUUID);
  }
}
