package io.weaviate.client6.v1.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Multi2VecClipVectorizer extends Vectorizer {
  @SerializedName("multi2vec-clip")
  private Map<String, Object> configuration;

  public static Multi2VecClipVectorizer of() {
    return new Builder().build();
  }

  public static Multi2VecClipVectorizer of(Consumer<Builder> fn) {
    var builder = new Builder();
    fn.accept(builder);
    return builder.build();
  }

  public static class Builder {
    private boolean vectorizeCollectionName = false;
    private String inferenceUrl;
    private Map<String, Float> imageFields = new HashMap<>();
    private Map<String, Float> textFields = new HashMap<>();

    public Builder inferenceUrl(String inferenceUrl) {
      this.inferenceUrl = inferenceUrl;
      return this;
    }

    public Builder imageFields(String... fields) {
      Arrays.stream(fields).forEach(f -> imageFields.put(f, null));
      return this;
    }

    public Builder imageField(String field, float weight) {
      imageFields.put(field, weight);
      return this;
    }

    public Builder textFields(String... fields) {
      Arrays.stream(fields).forEach(f -> textFields.put(f, null));
      return this;
    }

    public Builder textField(String field, float weight) {
      textFields.put(field, weight);
      return this;
    }

    public Builder vectorizeCollectionName() {
      this.vectorizeCollectionName = true;
      return this;
    }

    public Multi2VecClipVectorizer build() {
      return new Multi2VecClipVectorizer(new HashMap<>() {
        {
          put("vectorizeClassName", vectorizeCollectionName);
          if (inferenceUrl != null) {
            put("inferenceUrl", inferenceUrl);
          }

          var _imageFields = new ArrayList<String>();
          var _imageWeights = new ArrayList<Float>();
          splitEntries(imageFields, _imageFields, _imageWeights);

          var _textFields = new ArrayList<String>();
          var _textWeights = new ArrayList<Float>();
          splitEntries(imageFields, _textFields, _textWeights);

          put("imageFields", _imageFields);
          put("textFields", _textFields);
          put("weights", Map.of(
              "imageWeights", _imageWeights,
              "textWeights", _textWeights));
        }
      });
    }

    private void splitEntries(Map<String, Float> map, List<String> keys, List<Float> values) {
      map.entrySet().forEach(entry -> {
        keys.add(entry.getKey());
        var value = entry.getValue();
        if (value != null) {
          values.add(value);
        }
      });
    }
  }
}
