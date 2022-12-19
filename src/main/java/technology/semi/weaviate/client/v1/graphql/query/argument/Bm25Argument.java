package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
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
  Float alpha;
  String[] properties;


  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    arg.add(String.format("query: \"%s\"", query));

    if (properties != null) {
      arg.add(String.format("properties: %s", Arrays.toString(properties)));
    }
    
    return String.format("bm25: {%s}", StringUtils.joinWith(" ", arg.toArray()));
  }
}
