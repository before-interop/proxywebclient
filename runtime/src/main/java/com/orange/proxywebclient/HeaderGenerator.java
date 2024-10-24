package com.orange.proxywebclient;

import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public interface HeaderGenerator {

  default void generate(String name, Partner.Header options, HttpRequest<Buffer> request) {
    // do nothing
  }

  default void generate(
      String name,
      Partner.Header options,
      HttpRequest<Buffer> request,
      AuthenticationMethod caller) {
    generate(name, options, request);
  }

  public static class NotImplementedException extends RuntimeException {
    public NotImplementedException(String className) {
      super("No generator found for " + className);
    }
  }

  @ApplicationScoped
  @Unremovable
  public static class Loader {

    private static final HashMap<String, Class<?>> classList = new HashMap<>();
    private static final List<String> packageList = new ArrayList<>();

    @Inject ProxyConfig config;

    public HeaderGenerator get(String generatorName) {
      return (HeaderGenerator) Arc.container().instance(getClass(generatorName)).get();
    }

    private List<String> getPackageList() {
      if (!packageList.isEmpty()) {
        return packageList;
      }

      var packages = config.header.packages.orElse("");
      packages += ",,com.orange.proxywebclient.impl.header";

      Arrays.stream(packages.split(","))
          .distinct()
          .map(s -> s.isBlank() ? s : s + ".")
          .forEach(packageList::add);

      return packageList;
    }

    private Class<?> getClass(String className) {
      if (classList.containsKey(className)) {
        return classList.get(className);
      }

      for (var p : getPackageList()) {
        try {
          var c = Class.forName(p + className);
          if (HeaderGenerator.class.isAssignableFrom(c)) {
            classList.put(className, c);
            return c;
          }
        } catch (Exception e) {
          // ignore
        }
      }

      throw new NotImplementedException("Class not found: " + className);
    }
  }
}
