package technology.semi.weaviate.client.v1.schema.model;

import com.google.gson.annotations.SerializedName;

public enum DataType {
  @SerializedName("cref")
  CREF,
  @SerializedName("string")
  STRING,
  @SerializedName("text")
  TEXT,
  @SerializedName("int")
  INT,
  @SerializedName("number")
  NUMBER,
  @SerializedName("boolean")
  BOOLEAN,
  @SerializedName("date")
  DATE,
  @SerializedName("geoCoordinates")
  GEO_COORDINATES,
  @SerializedName("phoneNumber")
  PHONE_NUMBER
}
