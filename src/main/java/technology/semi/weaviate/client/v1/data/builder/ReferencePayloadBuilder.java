package technology.semi.weaviate.client.v1.data.builder;

import java.util.Objects;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.v1.data.model.SingleRef;

public class ReferencePayloadBuilder {

  private final BeaconPath beaconPath;
  private String id;
  private String className;

  @Deprecated
  public ReferencePayloadBuilder() {
    this.beaconPath = null;
    System.err.println("WARNING: Deprecated constructor for ReferencePayloadBuilder class was used. Please use parametrized one.");
  }

  public ReferencePayloadBuilder(BeaconPath beaconPath) {
    this.beaconPath = Objects.requireNonNull(beaconPath);
  }

  public ReferencePayloadBuilder withID(String id) {
    this.id = id;
    return this;
  }

  public ReferencePayloadBuilder withClassName(String className) {
    this.className = className;
    return this;
  }

  public SingleRef payload() {
    String beacon;
    if (beaconPath != null) {
      beacon = beaconPath.buildSingle(BeaconPath.Params.builder()
        .id(id)
        .className(className)
        .build());
    } else {
      beacon = beaconDeprecated();
    }

    return SingleRef.builder().beacon(beacon).build();
  }

  private String beaconDeprecated() {
    return String.format("weaviate://localhost/%s", id);
  }
}
