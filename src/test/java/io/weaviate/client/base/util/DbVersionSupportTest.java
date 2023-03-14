package io.weaviate.client.base.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class DbVersionSupportTest {

  private AutoCloseable openedMocks;
  @InjectMocks
  private DbVersionSupport dbVersionSupport;
  @Mock
  private DbVersionProvider dbVersionProviderMock;

  @Before
  public void setUp() {
    openedMocks = MockitoAnnotations.openMocks(this);
  }
  @After
  public void tearDown() throws Exception {
    openedMocks.close();
  }

  @Test
  @DataMethod(source = DbVersionSupportTest.class, method = "provideNotSupported")
  public void shouldNotSupport(String dbVersion) {
    Mockito.when(dbVersionProviderMock.getVersion()).thenReturn(dbVersion);

    assertThat(dbVersionSupport.supportsClassNameNamespacedEndpoints()).isFalse();
  }

  public static Object[][] provideNotSupported() {
    return new Object[][]{
      {"0.11"},
      {"1.13.9"},
      {"1.13"},
      {"1.0"},
    };
  }

  @Test
  @DataMethod(source = DbVersionSupportTest.class, method = "provideSupported")
  public void shouldSupport(String dbVersion) {
    Mockito.when(dbVersionProviderMock.getVersion()).thenReturn(dbVersion);

    assertThat(dbVersionSupport.supportsClassNameNamespacedEndpoints()).isTrue();
  }

  public static Object[][] provideSupported() {
    return new Object[][]{
      {"1.14.0"},
      {"1.14.9"},
      {"1.100"},
      {"2.0"},
      {"10.11.12"},
    };
  }
}
