package io.weaviate.client6.v1.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Test;

public class BuildInfoTest {
  private static final String BRANCH = gitBranch();
  private static final String COMMIT_ID = gitCommit();

  @Test
  public void testBuildInfo() throws IOException {
    Assume.assumeNotNull(BRANCH, COMMIT_ID);
    Assume.assumeTrue("found git branch", !BRANCH.isBlank());
    Assume.assumeTrue("found git commit", !COMMIT_ID.isBlank());

    Assertions.assertThat(BuildInfo.BRANCH).as("branch").isEqualTo(BRANCH);
    Assertions.assertThat(BuildInfo.COMMIT_ID).as("commit.full").isEqualTo(COMMIT_ID);
    Assertions.assertThat(COMMIT_ID).as("commit.abbrev").startsWith(BuildInfo.COMMIT_ID_ABBREV);
    Assertions.assertThat(BuildInfo.VERSION).as("version").isNotEmpty();
  }

  /** Get current non-abbreviated Git commit hash. */
  private static String gitCommit() {
    return runCommand("/usr/bin/git", "rev-parse", "HEAD");
  }

  /** Get current git branch. */
  private static String gitBranch() {
    return runCommand("/usr/bin/git", "branch", "--show-current");
  }

  /** Run shell command and return the output as multi-line string. */
  private static String runCommand(String... cmdarray) {
    try {
      var process = Runtime.getRuntime().exec(cmdarray);
      var r = new BufferedReader(new InputStreamReader(process.getInputStream()));
      return String.join("\n", (Iterable<String>) () -> r.lines().iterator());
    } catch (IOException e) {
      return null;
    }
  }
}
