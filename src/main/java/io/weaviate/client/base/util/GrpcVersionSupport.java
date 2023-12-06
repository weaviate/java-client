package io.weaviate.client.base.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class GrpcVersionSupport {

  private final DbVersionProvider provider;

  public boolean supportsVectorBytesField() {
    String[] versionNumbers = StringUtils.split(provider.getVersion(), ".");
    if (versionNumbers != null && versionNumbers.length >= 2) {
      int major = Integer.parseInt(versionNumbers[0]);
      int minor = Integer.parseInt(versionNumbers[1]);
      if (major == 1 && minor == 22 && versionNumbers.length == 3) {
        String patch = versionNumbers[2];
        if (!patch.contains("rc") && Integer.parseInt(patch) >= 6) {
          return true;
        }
      }
      return (major == 1 && minor >= 23) || major >= 2;
    }
    return false;
  }
}
