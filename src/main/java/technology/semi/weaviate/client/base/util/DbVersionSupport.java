package technology.semi.weaviate.client.base.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
@Getter
public class DbVersionSupport {

  private final String dbVersion;

  // supported since 1.14
  public boolean supportsClassNameNamespacedEndpoints() {
    String[] versionNumbers = StringUtils.split(dbVersion, ".");
    if (versionNumbers != null && versionNumbers.length >= 2) {
      int major = Integer.parseInt(versionNumbers[0]);
      int minor = Integer.parseInt(versionNumbers[1]);
      return (major == 1 && minor >= 14) || major >= 2;
    }
    return false;
  }

  public void warnDeprecatedNonClassNameNamespacedEndpointsForObjects() {
    System.err.printf("WARNING: Usage of objects paths without className is deprecated in Weaviate %s." +
      " Please provide className parameter\n", dbVersion);
  }

  public void warnDeprecatedNonClassNameNamespacedEndpointsForReferences() {
    System.err.printf("WARNING: Usage of references paths without className is deprecated in Weaviate %s." +
      " Please provide className parameter\n", dbVersion);
  }

  public void warnDeprecatedNonClassNameNamespacedEndpointsForBeacons() {
    System.err.printf("WARNING: Usage of beacon paths without className is deprecated in Weaviate %s." +
      " Please provide className parameter\n", dbVersion);
  }

  public void warnNotSupportedClassNamespacedEndpointsForObjects() {
    System.err.printf("WARNING: Usage of objects paths with className is not supported in Weaviate %s." +
      " className parameter is ignored\n", dbVersion);
  }

  public void warnNotSupportedClassNamespacedEndpointsForReferences() {
    System.err.printf("WARNING: Usage of references paths with className is not supported in Weaviate %s." +
      " className parameter is ignored\n", dbVersion);
  }

  public void warnNotSupportedClassNamespacedEndpointsForBeacons() {
    System.err.printf("WARNING: Usage of beacons paths with className is not supported in Weaviate %s." +
      " className parameter is ignored\n", dbVersion);
  }
}
