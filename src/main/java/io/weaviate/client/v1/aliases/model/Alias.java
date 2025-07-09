package io.weaviate.client.v1.aliases.model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Alias {
  @SerializedName("class")
  private final String collection;
  @SerializedName("alias")
  private final String alias;
}
