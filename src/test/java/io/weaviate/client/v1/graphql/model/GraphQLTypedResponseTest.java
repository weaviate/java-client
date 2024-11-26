package io.weaviate.client.v1.graphql.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import io.weaviate.client.base.Serializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.Getter;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class GraphQLTypedResponseTest {

  @Test
  public void testGraphQLGetResponse() throws IOException {
    // given
    Serializer s = new Serializer();
    String json = new String(Files.readAllBytes(Paths.get("src/test/resources/json/graphql-response.json")));
    // when
    Type responseType = TypeToken.getParameterized(GraphQLTypedResponse.class, Soups.class).getType();
    GraphQLTypedResponse<Soups> resp = s.toResponse(json, responseType);
    //
    assertThat(resp).isNotNull()
      .extracting(o -> o.getData().getObjects().getSoups())
      .extracting(o -> o.get(0)).isNotNull()
      .extracting(Soups.Soup::getName).isEqualTo("JustSoup");
  }

  @Test
  public void testGraphQLGetResponseSoups() throws IOException {
    // given
    Serializer s = new Serializer();
    String json = new String(Files.readAllBytes(Paths.get("src/test/resources/json/graphql-response.json")));
    // when
    GraphQLTypedResponse<Soups> resp = s.toGraphQLTypedResponse(json, Soups.class);
    //
    assertThat(resp).isNotNull()
      .extracting(o -> o.getData().getObjects().getSoups())
      .extracting(o -> o.get(0)).isNotNull()
      .extracting(Soups.Soup::getName).isEqualTo("JustSoup");
  }

  @Test
  public void testGraphQLGetResponseSoups2() throws IOException {
    // given
    Serializer s = new Serializer();
    String json = new String(Files.readAllBytes(Paths.get("src/test/resources/json/graphql-group-by-response.json")));
    // when
    GraphQLTypedResponse<Passages> resp = s.toGraphQLTypedResponse(json, Passages.class);
    // then
    assertThat(resp).isNotNull()
      .extracting(o -> o.getData().getObjects().getPassages())
      .extracting(o -> o.get(0)).isNotNull()
      .extracting(GraphQLGetBaseObject::getAdditional).isNotNull()
      .extracting(GraphQLGetBaseObject.Additional::getGroup).isNotNull()
      .extracting(GraphQLGetBaseObject.Additional.Group::getHits).isNotNull()
      .extracting(o -> o.get(0)).isNotNull()
      .extracting(GraphQLGetBaseObject.Additional.Group.GroupHit::getProperties).isNotNull()
      .extracting(o -> o.get("name")).isEqualTo("test-name");
  }
}

@Getter
class Soups {
  @SerializedName(value = "Soup")
  List<Soup> soups;

  @Getter
  public static class Soup extends GraphQLGetBaseObject {
    String name;
  }
}

@Getter
class Passages {
  @SerializedName(value = "Passage")
  List<Passage> passages;

  @Getter
  public static class Passage extends GraphQLGetBaseObject {
    String name;
  }
}
