## Dropwizard Ratelimit


Dropwizard Ratelimit adds the ability to apply rate limiting directly in [Dropwizard](http://www.dropwizard.io) without any external services.

This project uses [bucket4j](https://github.com/vladimir-bukhtoyarov/bucket4j) and [Hazelcast](https://hazelcast.org).

Pull requests are welcome!

#### How to use
This project works with Dropwizard 1.0.5.

Add the project to your dependencies. You will also need the following dependencies to your `pom.xml`:
```
<dependencies>
	<dependency>
		<groupId>com.hazelcast</groupId>
		<artifactId>hazelcast</artifactId>
		<version>3.7.5</version>
	</dependency>
	<dependency>
		<groupId>com.hazelcast</groupId>
		<artifactId>hazelcast-client</artifactId>
		<version>3.7.5</version>
	</dependency>
	<dependency>
		<groupId>com.github.vladimir-bukhtoyarov</groupId>
		<artifactId>bucket4j-jcache</artifactId>
		<version>2.0.0</version>
	</dependency>
	<dependency>
		<groupId>javax.cache</groupId>
		<artifactId>cache-api</artifactId>
		<version>1.0.0</version>
	</dependency>
</dependencies>
```

Then, in your Dropwizard initialize method add the following:
```
@Override
public void initialize(Bootstrap<Configuration> bootstrap) {

	long permitsPerPeriod = 100;
	long period = 1;
	TimeUnit timeUnit = TimeUnit.MINUTES;

	RateLimitProvider rateLimitProvider = new HazelcastRateLimitProvider(
		permitsPerSecond,
		period,
		timeUnit
	);

	bootstrap.addBundle(new RateLimitBundle(rateLimitProvider));
}
```
Finally, add the following annotation to your rate limited routes:
```
@GET
@Path("index")
@RateLimited(cost = 1)
Response getIndex() {
	// etc.
}
```

#### Copyright and License
Copyright 2017 ZIVVER B.V.
Licensed under the [MIT License](https://opensource.org/licenses/MIT)
