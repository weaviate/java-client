package io.weaviate.containers;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class TestListener extends RunListener {

  @Override
  public void testRunFinished(Result result) throws Exception {
    Container.stopAll();
    super.testRunFinished(result);
  }

}
