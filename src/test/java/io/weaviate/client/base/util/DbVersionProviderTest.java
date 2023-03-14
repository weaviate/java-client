package io.weaviate.client.base.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DbVersionProviderTest {

  public static final String VERSION_1 = "1.2.3";
  public static final String VERSION_2 = "2.3.4";
  @InjectMocks
  private DbVersionProvider dbVersionProvider;
  @Mock
  private DbVersionProvider.VersionGetter versionGetterMock;


  @Test
  public void shouldReturnEmptyVersion() {
    Mockito.when(versionGetterMock.get()).thenReturn(Optional.empty());

    assertThat(dbVersionProvider.getVersion()).isEmpty();
  }

  @Test
  public void shouldReturnProperVersion() {
    Mockito.when(versionGetterMock.get()).thenReturn(Optional.of(VERSION_1));

    assertThat(dbVersionProvider.getVersion()).isEqualTo(VERSION_1);
  }

  @Test
  public void shouldReturnNewVersionAfterRefresh() {
    Mockito.when(versionGetterMock.get()).thenReturn(Optional.of(VERSION_1), Optional.of(VERSION_2));

    assertThat(dbVersionProvider.getVersion()).isEqualTo(VERSION_1);
    dbVersionProvider.refresh(true);
    assertThat(dbVersionProvider.getVersion()).isEqualTo(VERSION_2);
  }

  @Test
  public void shouldFetchVersionOnce() {
    Mockito.when(versionGetterMock.get()).thenReturn(Optional.of(VERSION_1));

    dbVersionProvider.getVersion();
    dbVersionProvider.getVersion();
    dbVersionProvider.getVersion();

    Mockito.verify(versionGetterMock, Mockito.times(1)).get();
  }

  @Test
  public void shouldFetchVersionUntilSuccess() {
    Mockito.when(versionGetterMock.get()).thenReturn(Optional.empty(), Optional.empty(), Optional.of(VERSION_1));

    assertThat(dbVersionProvider.getVersion()).isEmpty();
    assertThat(dbVersionProvider.getVersion()).isEmpty();
    assertThat(dbVersionProvider.getVersion()).isEqualTo(VERSION_1);
    assertThat(dbVersionProvider.getVersion()).isEqualTo(VERSION_1);
    assertThat(dbVersionProvider.getVersion()).isEqualTo(VERSION_1);

    Mockito.verify(versionGetterMock, Mockito.times(3)).get();
  }

}
