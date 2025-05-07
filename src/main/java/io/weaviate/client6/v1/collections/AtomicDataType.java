package io.weaviate.client6.v1.collections;

import com.google.gson.annotations.SerializedName;

public enum AtomicDataType {
  @SerializedName("text")
  TEXT,
  @SerializedName("int")
  INT,
  @SerializedName("blob")
  BLOB;

  public static boolean isAtomic(String type) {
    return type.equals(TEXT.name().toLowerCase())
        || type.equals(INT.name().toLowerCase())
        || type.equals(BLOB.name().toLowerCase());
  }
}
