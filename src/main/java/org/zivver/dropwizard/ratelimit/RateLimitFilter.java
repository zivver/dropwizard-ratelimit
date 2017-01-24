package org.zivver.dropwizard.ratelimit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;

@RateLimited
public class RateLimitFilter implements ContainerRequestFilter {
  private final RateLimitProvider provider;

  @Context
  private ResourceInfo resourceInfo;

  @Context
  private HttpServletRequest request;

  public RateLimitFilter(RateLimitProvider provider) {
    this.provider = provider;
  }

  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    String id = "ip::" + request.getRemoteAddr();
    Method method = resourceInfo.getResourceMethod();
    long cost;
    if (method != null) {
      cost = method.getAnnotation(RateLimited.class).cost();
    } else {
      cost = 1L;
    }

    if (provider.isOverLimit(id, cost)) {
      context.abortWith(
        Response
          .status(429)
          .entity("{\"error\":\"Too Many Requests\"}")
          .type(MediaType.APPLICATION_JSON)
          .build()
      );
    }
  }
}
