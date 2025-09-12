package io.weaviate.client6.v1.api.collections;

import com.google.gson.annotations.SerializedName;

public enum Tokenization {
  @SerializedName("word")
  WORD,
  @SerializedName("whitespace")
  WHITESPACE,
  @SerializedName("lowercase")
  LOWERCASE,
  @SerializedName("field")
  FIELD,
  @SerializedName("gse")
  GSE,
  @SerializedName("trigram")
  TRIGRAM,
  @SerializedName("kagome_ja")
  KAGOME_JA,
  @SerializedName("kagome_kr")
  KAGOME_KR;
}
