package technology.semi.weaviate.client.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class Serializer {
  private Gson gson;

  public Serializer() {
    this.gson = new GsonBuilder().disableHtmlEscaping().create();
  }

  public <C> C toResponse(String response, Class<C> classOfT) {
    return gson.fromJson(response, classOfT);
  }

  public String toJsonString(Object object) {
    return (object != null) ? gson.toJson(object) : null;
  }
}
