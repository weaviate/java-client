package technology.semi.weaviate.client.base.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class DbVersionSupportTest {

    @Test
    @DataMethod(source = DbVersionSupportTest.class, method = "provideNotSupported")
    public void shouldNotSupport(String dbVersion) {
        DbVersionSupport support = new DbVersionSupport(dbVersion);
        assertThat(support.supportsClassNameNamespacedEndpoints()).isFalse();
    }

    public static Object[][] provideNotSupported() {
        return new Object[][] {
                {"0.11"},
                {"1.13.9"},
                {"1.13"},
                {"1.0"},
        };
    }

    @Test
    @DataMethod(source = DbVersionSupportTest.class, method = "provideSupported")
    public void shouldSupport(String dbVersion) {
        DbVersionSupport support = new DbVersionSupport(dbVersion);
        assertThat(support.supportsClassNameNamespacedEndpoints()).isTrue();
    }

    public static Object[][] provideSupported() {
        return new Object[][] {
                {"1.14.0"},
                {"1.14.9"},
                {"1.100"},
                {"2.0"},
                {"10.11.12"},
        };
    }
}