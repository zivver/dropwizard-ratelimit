package org.zivver.dropwizard.ratelimit;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitBundle implements Bundle {
  RateLimitProvider provider;

  public RateLimitBundle(RateLimitProvider provider) {
    this.provider = provider;
  }

  public void initialize(Bootstrap<?> bootstrap) {}

  public void run(Environment environment) {
    environment.jersey().register(new RateLimitFilter(provider));
  }
}
