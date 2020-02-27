package io.jooby.internal.openapi;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Optional;

public class SchemaRef {
  public final Schema schema;

  public final Optional<String> ref;

  public SchemaRef(Schema schema, String ref) {
    this.schema = schema;
    this.ref = Optional.ofNullable(ref);
  }

  public Schema toSchema() {
    return this.ref.map(ref -> new Schema().$ref(ref)).orElse(this.schema);
  }
}
