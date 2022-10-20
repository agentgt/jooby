/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package examples;

import io.jooby.Jooby;

public class MvcAppWithRoutes extends Jooby {

  {
    routes(() -> mvc(ControllerExample.class));
  }
}
