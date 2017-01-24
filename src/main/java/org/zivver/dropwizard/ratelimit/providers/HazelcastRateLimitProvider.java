package org.zivver.dropwizard.ratelimit.providers;

import com.github.bucket4j.BandwidthDefinition;
import com.github.bucket4j.Bucket;
import com.github.bucket4j.BucketConfiguration;
import com.github.bucket4j.TimeMeter;
import com.github.bucket4j.grid.GridBucket;
import com.github.bucket4j.grid.GridBucketState;
import com.github.bucket4j.grid.hazelcast.HazelcastProxy;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.zivver.dropwizard.ratelimit.RateLimitProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HazelcastRateLimitProvider implements RateLimitProvider {
  final static protected String HAZELCAST_RATELIMIT_MAP = "org.zivver.dropwizard.ratelimit";
  final static protected TimeMeter TIME_METER = TimeMeter.SYSTEM_MILLISECONDS;

  protected BucketConfiguration bucketConfiguration;
  protected IMap<Object, GridBucketState> buckets;

  public HazelcastRateLimitProvider(long permitsPerMinute) {
    this(permitsPerMinute, 1, TimeUnit.MINUTES);
  }

  public HazelcastRateLimitProvider(long permitsPerPeriod, long period, TimeUnit timeUnit) {
    this(permitsPerPeriod, period, timeUnit, null);
  }

  public HazelcastRateLimitProvider(long permitsPerPeriod, long period, TimeUnit timeUnit, Config config) {
    long bandwidthPeriod = TIME_METER.toBandwidthPeriod(timeUnit, period);
    List<BandwidthDefinition> bandwidths = new ArrayList<>();
    bandwidths.add(new BandwidthDefinition(permitsPerPeriod, permitsPerPeriod, bandwidthPeriod, false));
    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

    this.bucketConfiguration = new BucketConfiguration(bandwidths, TIME_METER);
    this.buckets = hazelcastInstance.getMap(HAZELCAST_RATELIMIT_MAP);

  }

  public boolean isOverLimit(String id, long cost) {
    Bucket bucket = new GridBucket(bucketConfiguration, new HazelcastProxy(buckets, id));
    return !bucket.tryConsume(cost);
  }
}
