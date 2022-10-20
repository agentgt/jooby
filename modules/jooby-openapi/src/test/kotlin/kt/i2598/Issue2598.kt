/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package kt.i2598

import io.jooby.Context
import io.jooby.openapi.OpenAPITest
import io.jooby.openapi.RouteIterator
import org.junit.jupiter.api.Assertions.assertEquals

class Issue2598 {

  @OpenAPITest(value = App2598::class)
  fun shouldParseTypeWithoutError(iterator: RouteIterator) {
    iterator
      .next { route -> assertEquals(Context::class.java.name, route.defaultResponse.javaType) }
      .verify()
  }
}
