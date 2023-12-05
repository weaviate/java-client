package io.weaviate.client.base.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JParamsTestRunner.class)
public class GrpcVersionSupportTest {

  private AutoCloseable openedMocks;
  @InjectMocks
  private GrpcVersionSupport grpcVersionProvider;
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
  @DataMethod(source = GrpcVersionSupportTest.class, method = "provideNotSupported")
  public void shouldNotSupportVectorBytes(String dbVersion) {
    Mockito.when(dbVersionProviderMock.getVersion()).thenReturn(dbVersion);

    assertThat(grpcVersionProvider.supportsVectorBytesField()).isFalse();
  }

  public static Object[][] provideNotSupported() {
    return new Object[][]{
      {"0.11"},
      {"1.13.9"},
      {"1.22.0-rc.0"},
      {"1.22.4"},
      {"1.22.5"},
    };
  }

  @Test
  @DataMethod(source = GrpcVersionSupportTest.class, method = "provideSupported")
  public void shouldSupportVectorBytes(String dbVersion) {
    Mockito.when(dbVersionProviderMock.getVersion()).thenReturn(dbVersion);

    assertThat(grpcVersionProvider.supportsVectorBytesField()).isTrue();
  }

  public static Object[][] provideSupported() {
    return new Object[][]{
      {"1.22.6"},
      {"1.23.0-rc.0"},
      {"1.23.10"},
      {"1.30.1"},
      {"1.31"},
      {"2.31"},
      {"10.11.12"},
    };
  }
}
