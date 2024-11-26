package io.weaviate.client.base.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.weaviate.client.v1.graphql.model.GraphQLGetBaseObject;
import java.lang.reflect.Type;
import java.util.Map;

public class GroupHitDeserializer implements JsonDeserializer<GraphQLGetBaseObject.Additional.Group.GroupHit> {

  @Override
  public GraphQLGetBaseObject.Additional.Group.GroupHit deserialize(JsonElement json, Type typeOfT,
    JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    GraphQLGetBaseObject.Additional.Group.GroupHit.AdditionalGroupHit additional =
      context.deserialize(jsonObject.get("_additional"), GraphQLGetBaseObject.Additional.Group.GroupHit.AdditionalGroupHit.class);

    // Remove _additional from the JSON object
    jsonObject.remove("_additional");

    // Deserialize the rest into a Map
    Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> properties = context.deserialize(jsonObject, mapType);

    return new GraphQLGetBaseObject.Additional.Group.GroupHit(properties, additional);
  }
}
