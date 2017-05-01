package org.zivver.dropwizard.ratelimit.providers;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICacheManager;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import org.zivver.dropwizard.ratelimit.RateLimitProvider;

import javax.cache.Cache;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class HazelcastRateLimitProvider implements RateLimitProvider {

  public static final String BUCKET_CACHE = "my_buckets";
  private final ProxyManager<String> bucketManager;
  private Supplier<BucketConfiguration> configSupplier;

  public HazelcastRateLimitProvider(long permitsPerMinute) {
    this(permitsPerMinute, 1, TimeUnit.MINUTES);
  }

  public HazelcastRateLimitProvider(long permitsPerPeriod, long period, TimeUnit timeUnit) {
    this(permitsPerPeriod, period, timeUnit, null);
  }

  public HazelcastRateLimitProvider(long permitsPerPeriod, long period, TimeUnit timeUnit, Config config) {
    Duration periodDuration = Duration.ofNanos(timeUnit.toNanos(period));
    Bandwidth rateLimit = Bandwidth.simple(permitsPerPeriod, periodDuration);
    BucketConfiguration configuration = Bucket4j.configurationBuilder()
            .addLimit(rateLimit)
            .buildConfiguration();
    this.configSupplier = () -> configuration;

    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    ICacheManager cacheManager = hazelcastInstance.getCacheManager();
    Cache<String, GridBucketState> cache = cacheManager.getCache(BUCKET_CACHE);
    this.bucketManager = Bucket4j.extension(JCache.class).proxyManagerForCache(cache);
  }

  public boolean isOverLimit(String id, long cost) {
    Bucket bucket = bucketManager.getProxy(id, configSupplier);
    return !bucket.tryConsume(cost);
  }
}
