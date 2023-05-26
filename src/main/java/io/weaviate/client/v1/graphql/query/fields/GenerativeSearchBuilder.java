package io.weaviate.client.v1.graphql.query.fields;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GenerativeSearchBuilder {

  String singleResultPrompt;
  String groupedResultTask;
  String[] groupedResultProperties;

  public Field build() {
    Set<String> nameParts = new LinkedHashSet<>();
    Set<String> fieldNames = new LinkedHashSet<>();

    if (StringUtils.isNotBlank(singleResultPrompt)) {
      nameParts.add(String.format("singleResult:{prompt:\"\"%s\"\"}", Serializer.quote(singleResultPrompt)));
      fieldNames.add("singleResult");
    }
    if (StringUtils.isNotBlank(groupedResultTask) || ArrayUtils.isNotEmpty(groupedResultProperties)) {
      Set<String> argParts = new LinkedHashSet<>();
      if (StringUtils.isNotBlank(groupedResultTask)) {
        argParts.add(String.format("task:\"\"%s\"\"", Serializer.quote(groupedResultTask)));
      }
      if (ArrayUtils.isNotEmpty(groupedResultProperties)) {
        argParts.add(String.format("properties:%s", Serializer.arrayWithQuotes(groupedResultProperties)));
      }
      nameParts.add(String.format("groupedResult:{%s}", StringUtils.join(argParts, " ")));
      fieldNames.add("groupedResult");
    }

    if (nameParts.isEmpty()) {
      return  Field.builder().build();
    }

    fieldNames.add("error");
    String name = String.format("generate(%s)", String.join(" ", nameParts));
    Field[] fields = fieldNames.stream()
      .map(n -> Field.builder().name(n).build())
      .toArray(Field[]::new);

    return Field.builder().name(name).fields(fields).build();
  }
}
