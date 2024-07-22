package io.weaviate.client.base.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class DbVersionSupport {

  private final DbVersionProvider provider;

  // supported since 1.14
  public boolean supportsClassNameNamespacedEndpoints() {
    return satisfiesVersion(1, 14);
  }

  public boolean supportsOnly100TenantsInOneRequest() {
    return satisfiesVersion(1, 26);
  }

  private boolean satisfiesVersion(int major, int minor) {
    String[] versionNumbers = StringUtils.split(provider.getVersion(), ".");
    if (versionNumbers != null && versionNumbers.length >= 2) {
      int parsedMajor = Integer.parseInt(versionNumbers[0]);
      int parsedMinor = Integer.parseInt(versionNumbers[1]);
      return (parsedMajor == major && parsedMinor >= minor) || parsedMajor >= 2;
    }
    return false;
  }

  public void warnDeprecatedNonClassNameNamespacedEndpointsForObjects() {
    System.err.printf("WARNING: Usage of objects paths without className is deprecated in Weaviate %s." +
      " Please provide className parameter\n", provider.getVersion());
  }

  public void warnDeprecatedNonClassNameNamespacedEndpointsForReferences() {
    System.err.printf("WARNING: Usage of references paths without className is deprecated in Weaviate %s." +
      " Please provide className parameter\n", provider.getVersion());
  }

  public void warnDeprecatedNonClassNameNamespacedEndpointsForBeacons() {
    System.err.printf("WARNING: Usage of beacon paths without className is deprecated in Weaviate %s." +
      " Please provide className parameter\n", provider.getVersion());
  }

  public void warnNotSupportedClassNamespacedEndpointsForObjects() {
    System.err.printf("WARNING: Usage of objects paths with className is not supported in Weaviate %s." +
      " className parameter is ignored\n", provider.getVersion());
  }
  public void warnNotSupportedClassParameterInEndpointsForObjects() {
    System.err.printf("WARNING: Usage of objects paths with class query parameter is not supported in Weaviate %s." +
      " class query parameter is ignored\n", provider.getVersion());
  }

  public void warnNotSupportedClassNamespacedEndpointsForReferences() {
    System.err.printf("WARNING: Usage of references paths with className is not supported in Weaviate %s." +
      " className parameter is ignored\n", provider.getVersion());
  }

  public void warnNotSupportedClassNamespacedEndpointsForBeacons() {
    System.err.printf("WARNING: Usage of beacons paths with className is not supported in Weaviate %s." +
      " className parameter is ignored\n", provider.getVersion());
  }
}
