package com.orange.proxywebclient.impl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

@RegisterForReflection
public class ConfigPartnerAuth {

  @NotBlank protected String login;
  @NotBlank protected String password;

  protected Optional<String> tokenUrl = Optional.empty();

  protected Optional<String> grantType = Optional.empty();

  public Optional<String> getGrantType() {
    return grantType;
  }

  public void setGrantType(String grantType) {
    this.grantType = Optional.of(grantType);
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Optional<String> getTokenUrl() {
    return tokenUrl;
  }

  public ConfigPartnerAuth setTokenUrl(String tokenUrl) {
    this.tokenUrl = Optional.of(tokenUrl);
    return this;
  }
}
