package io.weaviate.client.v1.misc.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiVectorConfig {
  @Builder.Default
  private boolean enabled = true;
  @Builder.Default
  private Aggregation aggregation = Aggregation.MAX_SIM;
  @SerializedName("muvera")
  private MuveraConfig muvera;

  public enum Aggregation {
    MAX_SIM;
  }


  public MuveraConfig getMuveraEncoding() {
    return this.muvera;
  }

  private MuveraConfig getMuvera() {
    return this.muvera;
  }

  public static class MultiVectorConfigBuilder {
    private MuveraConfig muvera;

    public MultiVectorConfigBuilder encoding(MuveraConfig muvera) {
      this.muvera = muvera;
      return this;
    }

    private MultiVectorConfigBuilder muvera(MuveraConfig _muvera) {
      return this;
    }

  }
}
