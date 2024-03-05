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
    String json = new String(Files.readAllBytes(Paths.get("src/test/resources/json/graphql-response.json")));
    // when
    GraphQLTypedResponse<Soups2> resp = s.toGraphQLTypedResponse(json, Soups2.class);
    // then
    assertThat(resp).isNotNull()
      .extracting(o -> o.getData().getObjects().getSoups())
      .extracting(o -> o.get(0)).isNotNull()
      .extracting(Soups2.Soup::getName).isEqualTo("JustSoup");
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


class Soups2 {
  @SerializedName(value = "Soup")
  List<Soup> soups;

  public List<Soup> getSoups() {
    return soups;
  }

  public static class Soup extends GraphQLGetBaseObject {
    String name;
    Float price;

    public String getName() {
      return name;
    }

    public Float getPrice() {
      return price;
    }
  }
}
