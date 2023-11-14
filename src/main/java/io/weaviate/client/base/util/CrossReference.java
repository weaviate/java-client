package io.weaviate.client.base.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@ToString
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CrossReference {
  String peerName;
  String className;
  String targetID;
  boolean local;

  public CrossReference(String peerName, String className, String targetID) {
    this.local = peerName != null && peerName.equals("localhost");
    this.peerName = peerName;
    this.className = className;
    this.targetID = targetID;
  }

  public static CrossReference fromBeacon(String beacon) {
    if (StringUtils.isNotBlank(beacon) && beacon.startsWith("weaviate://")) {
      String path = beacon.replaceFirst("weaviate://", "");
      String[] parts = path.split("/");
      if (parts.length == 3) {
        return new CrossReference(parts[0], parts[1], parts[2]);
      }
      if (parts.length == 2) {
        return new CrossReference(parts[0], "", parts[1]);
      }
    }
    return null;
  }
}
