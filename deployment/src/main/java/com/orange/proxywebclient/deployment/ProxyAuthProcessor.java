package com.orange.proxywebclient.deployment;

import com.orange.proxywebclient.ProxyAuthMethod;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.jandex.DotName;

class ProxyAuthProcessor {

  private static final String FEATURE = "proxywebclient";

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  void registerAnnotations(BuildProducer<BeanDefiningAnnotationBuildItem> producer) {
    producer.produce(
        new BeanDefiningAnnotationBuildItem(
            DotName.createSimple(ProxyAuthMethod.class.getName()),
            DotName.createSimple(RequestScoped.class.getName()),
            false));
  }
}
