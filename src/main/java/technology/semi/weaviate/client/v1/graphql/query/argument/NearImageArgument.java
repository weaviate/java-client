package technology.semi.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearImageArgument implements Argument {
  String image;
  File imageFile;
  Float certainty;
  Float distance;

  private String readFile(File file) {
    try {
      byte[] content = Files.readAllBytes(Paths.get(file.toURI()));
      return Base64.getEncoder().encodeToString(content);
    } catch (Exception e) {
      return null;
    }
  }

  private String getContent() {
    if (StringUtils.isNotBlank(image)) {
      if (image.startsWith("data:")) {
        String base64 = ";base64,";
        return image.substring(image.indexOf(base64) + base64.length());
      }
      return image;
    }
    if (imageFile != null) {
      return readFile(imageFile);
    }
    return null;
  }

  @Override
  public String build() {
    Set<String> fields = new LinkedHashSet<>();
    String content = getContent();
    if (StringUtils.isNotBlank(content)) {
      fields.add(String.format("image: \"%s\"", content));
    }
    if (certainty != null) {
      fields.add(String.format("certainty: %s", certainty));
    }
    if (distance != null) {
      fields.add(String.format("distance: %s", distance));
    }
    return String.format("nearImage: {%s}", StringUtils.joinWith(" ", fields.toArray()));
  }
}
