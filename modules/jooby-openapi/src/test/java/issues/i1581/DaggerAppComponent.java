/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package issues.i1581;

public class DaggerAppComponent {
  public static DaggerBuilder builder() {
    return new DaggerBuilder();
  }
}
