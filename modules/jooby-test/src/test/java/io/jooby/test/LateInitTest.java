/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package io.jooby.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.jooby.Jooby;

public class LateInitTest {

  @Test
  public void defaultLateInit() {
    Jooby app = new Jooby();
    AtomicBoolean run = new AtomicBoolean();
    app.install(router -> run.set(true));
    assertEquals(true, run.get());
  }

  @Test
  public void lateInitOn() {
    Jooby app = new Jooby();
    AtomicBoolean run = new AtomicBoolean();
    app.setLateInit(true);
    app.install(router -> run.set(true));
    assertEquals(false, run.get());
  }
}
