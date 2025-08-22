package io.weaviate.client6.v1.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BuildInfoTest {

  @Test
  public void testBuildInfo() throws IOException {
    Assertions.assertThat(BuildInfo.BRANCH).as("branch").isEqualTo(gitBranch());
    Assertions.assertThat(BuildInfo.COMMIT_ID).as("commit.full").isEqualTo(gitCommit());
    Assertions.assertThat(gitCommit()).as("commit.abbrev").startsWith(BuildInfo.COMMIT_ID_ABBREV);
  }

  /** Get current non-abbreviated Git commit hash. */
  private static String gitCommit() throws IOException {
    return runCommand("git", "rev-parse", "HEAD");
  }

  /** Get current git branch. */
  private static String gitBranch() throws IOException {
    return runCommand("git", "branch", "--show-current");
  }

  /** Run shell command and return the output as multi-line string. */
  private static String runCommand(String... cmdarray) throws IOException {
    var process = Runtime.getRuntime().exec(cmdarray);
    var r = new BufferedReader(new InputStreamReader(process.getInputStream()));
    return String.join("\n", (Iterable<String>) () -> r.lines().iterator());
  }
}
