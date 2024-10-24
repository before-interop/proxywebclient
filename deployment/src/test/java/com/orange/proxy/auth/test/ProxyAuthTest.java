package com.orange.proxy.auth.test;

import com.orange.proxywebclient.AuthenticationMethodOauth2;
import io.quarkus.test.QuarkusUnitTest;
import java.time.Instant;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProxyAuthTest {

  // Start unit test with your extension loaded
  @RegisterExtension
  static final QuarkusUnitTest unitTest =
      new QuarkusUnitTest().setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Test
  public void exiparationTokenTest() {
    var now = Instant.now().getEpochSecond();
    var token = new AuthenticationMethodOauth2.Token();
    var tolerance = 3;

    Assertions.assertTrue(
        AuthenticationMethodOauth2.isExpired(token.setExp(now - 500)), "Token should be expired");

    Assertions.assertTrue(
        AuthenticationMethodOauth2.isExpired(
            token.setExp(now + AuthenticationMethodOauth2.TOKEN_EXPIRATION_MARGIN - tolerance)),
        "Token should be expired caused by tolerance");

    Assertions.assertTrue(
        AuthenticationMethodOauth2.isExpired(token.setExp(now)),
        "Token should be expired caused by tolerance");

    Assertions.assertFalse(
        AuthenticationMethodOauth2.isExpired(
            token.setExp(now + AuthenticationMethodOauth2.TOKEN_EXPIRATION_MARGIN + tolerance)),
        "Token should not be expired");
  }
}
