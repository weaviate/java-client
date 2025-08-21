package io.weaviate.client6.v1.internal;

import java.time.Instant;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

@RunWith(JParamsTestRunner.class)
public class TokenTest {

  public static Object[][] testCaseTokens() {
    return new Object[][] {
        { Token.expireNever("access_token"), true },
        { Token.expireAfter("access_token", "refresh_token", 100), true },
        { Token.expireAfter("access_token", 100), true },
        { new Token("access_token", "refresh_token", Instant.now().minusSeconds(10), 5, 0), false },
        { Token.expireAfter("access_token", 0), false },
        { Token.expireAfter("access_token", 5).withExpiryDelta(10), false },
        { Token.expireAfter("access_token", 100).withExpiryDelta(10), true },
    };
  }

  @DataMethod(source = TokenTest.class, method = "testCaseTokens")
  @Test
  public void test_isValid(Token token, boolean wantValid) {
    Assertions.assertThat(token.isValid())
        .as(token.toString())
        .isEqualTo(wantValid);
  }

  @Test
  public void test_expiryDeltaNonNegative() {
    var t = Token.expireAfter("access_token", 90L);
    var expireLater = t.withExpiryDelta(-10L);
    Assertions.assertThat(expireLater.expiryDelta()).as("expiryDelta must be >= 0").isEqualTo(0L);
  }
}
