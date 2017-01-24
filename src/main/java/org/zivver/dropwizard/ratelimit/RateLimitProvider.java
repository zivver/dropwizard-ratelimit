package org.zivver.dropwizard.ratelimit;

public interface RateLimitProvider {
  public abstract boolean isOverLimit(String id, long cost);
}
