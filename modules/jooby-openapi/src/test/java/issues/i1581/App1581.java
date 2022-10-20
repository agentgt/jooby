/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package issues.i1581;

import io.jooby.Jooby;

public class App1581 extends Jooby {
  {
    AppComponent dagger = DaggerAppComponent.builder().build();

    mvc(dagger.myController());
  }
}
