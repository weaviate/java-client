package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
class NearMediaArgumentHelper {

  String mediaName;
  String mediaField;
  String data;
  File dataFile;
  Float certainty;
  Float distance;
  String[] targetVectors;
  Targets targets;


  public String build() {
    Set<String> fields = new LinkedHashSet<>();

    String content = getContent();
    if (StringUtils.isNotBlank(content)) {
      fields.add(String.format("%s:%s", mediaField, Serializer.quote(content)));
    }
    if (certainty != null) {
      fields.add(String.format("certainty:%s", certainty));
    }
    if (distance != null) {
      fields.add(String.format("distance:%s", distance));
    }
    if (ArrayUtils.isNotEmpty(targetVectors)) {
      fields.add(String.format("targetVectors:%s",  Serializer.arrayWithQuotes(targetVectors)));
    }
    if (targets != null) {
      fields.add(String.format("%s", targets.build()));
    }

    return String.format("%s:{%s}", mediaName, String.join(" ", fields));
  }

  private String getContent() {
    if (StringUtils.isNotBlank(data)) {
      if (data.startsWith("data:")) {
        String base64 = ";base64,";
        return data.substring(data.indexOf(base64) + base64.length());
      }
      return data;
    }
    if (dataFile != null) {
      return readFile(dataFile);
    }
    return null;
  }

  private String readFile(File file) {
    try {
      byte[] content = Files.readAllBytes(Paths.get(file.toURI()));
      return Base64.getEncoder().encodeToString(content);
    } catch (Exception e) {
      return null;
    }
  }
}
