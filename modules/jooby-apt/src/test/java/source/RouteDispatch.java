/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package source;

import io.jooby.annotations.Dispatch;
import io.jooby.annotations.GET;
import io.jooby.annotations.Path;

@Dispatch
public class RouteDispatch {

  @Path("/toplevel")
  @GET
  public void toplevel() {}

  @Path("/methodlevel")
  @GET
  @Dispatch("single")
  public void methodlevel() {}
}
