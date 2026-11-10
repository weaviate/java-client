package io.weaviate.client6.v1.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.internal.VersionSupport.SemanticVersion;

@RunWith(JParamsTestRunner.class)
public class VersionSupportTest {
  public static Object[][] testCases() {
    return new Object[][] {
        { "1.31.6", "v1.32.1", true }, // can have a leading v
        { "v1.33.0", "1.32.1", false }, // can have a leading v
        { "2.36.2", "2.36.0-rc.3", true }, // patch ignored
        { "1.12", "1.11", false }, // omit patch
        { "0.55.6", "0.1.0", false }, // can start with zero
        { "1.32.0", "2.0.0", true }, // major version bump
        { "1.32.0", "2.5.1", true }, // major version bump
        { "1.32.0", "10.0.0", true }, // double-digit major
        { "1.32.0", "1.100.0", true }, // double-digit minor
        { "1.32.0", "1.31.9", false }, // older minor is still unsupported
    };
  }

  @Test
  @DataMethod(source = VersionSupportTest.class, method = "testCases")
  public void test_isSupported(String minimal, String actual, boolean isSupported) {
    var v_minimal = SemanticVersion.of(minimal);
    var v_actual = SemanticVersion.of(actual);

    if (isSupported) {
      Assertions.assertThat(v_actual)
          .describedAs("%s supported (minimal=%s)", actual, minimal)
          .isGreaterThanOrEqualTo(v_minimal);
    } else {
      Assertions.assertThat(v_actual)
          .describedAs("%s not supported (minimal=%s)", actual, minimal)
          .isLessThan(v_minimal);
    }
  }
}