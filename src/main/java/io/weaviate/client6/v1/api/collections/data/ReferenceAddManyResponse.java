package io.weaviate.client6.v1.api.collections.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public record ReferenceAddManyResponse(List<BatchError> errors) {
  public record BatchError(String message, BatchReference reference, int referenceIndex) {
  }

  public static enum CustomJsonDeserializer implements JsonDeserializer<ReferenceAddManyResponse> {
    INSTANCE;

    @Override
    public ReferenceAddManyResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      var errors = new ArrayList<BatchError>();
      int i = 0;
      for (var el : json.getAsJsonArray()) {
        var result = el.getAsJsonObject().get("result").getAsJsonObject();
        if (result.get("status").getAsString().equals("FAILED")) {
          var errorMsg = result
              .get("errors").getAsJsonObject()
              .get("error").getAsJsonArray()
              .get(0).getAsString();

          var batchErr = new BatchError(errorMsg, null, i);
          errors.add(batchErr);
        }
        i++;
      }
      return new ReferenceAddManyResponse(errors);
    }
  }
}
