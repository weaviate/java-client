package technology.semi.weaviate.client.v1.graphql.query.argument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Bm25Argument implements Argument {
  String query;
  String[] properties;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    arg.add(String.format("query: \"%s\"", query));

    if (properties != null) {
      arg.add(String.format("properties: %s", toJsonString(properties)));
    }

    return String.format("bm25: {%s}", StringUtils.joinWith(" ", arg.toArray()));
  }

  private String toJsonString(Object object) {
    Gson serializer = new GsonBuilder().disableHtmlEscaping().create();
    return serializer.toJson(object);
  }
}
