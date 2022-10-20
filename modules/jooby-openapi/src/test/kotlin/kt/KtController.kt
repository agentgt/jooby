/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package kt

import examples.ABean
import jakarta.inject.Named
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.delay

class KtController {

  @GET
  fun doSomething(): String {
    return ""
  }

  @DELETE @Path("/unit") fun doUnit() {}

  @GET
  @Path("/doMap")
  fun doMap(): Map<String, Any> {
    return mapOf()
  }

  @Path("/doParams")
  fun params(
    @QueryParam("I") i: Int,
    @QueryParam("oI") oi: Int?,
    @QueryParam("q") q: String,
    @QueryParam("nullq") nullq: String?
  ): ABean {
    return ABean()
  }

  @GET
  @Path("/coroutine")
  suspend fun coroutine(): List<String> {
    delay(100)
    return listOf("...")
  }

  @GET
  @Path("/future")
  fun completableFuture(): CompletableFuture<String> {
    return CompletableFuture.completedFuture("...")
  }

  @GET
  @Path("/httpNames")
  fun httpNames(
    @HeaderParam("Last-Modified-Since") lastModifiedSince: String,
    @Named("x-search") @io.jooby.annotations.QueryParam q: String
  ): String {
    return lastModifiedSince
  }
}
