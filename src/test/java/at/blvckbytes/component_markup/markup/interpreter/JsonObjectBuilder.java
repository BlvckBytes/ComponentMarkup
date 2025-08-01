/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class JsonObjectBuilder implements JsonBuilder {

  private final JsonObject result = new JsonObject();

  public JsonObjectBuilder object(String key, Function<JsonObjectBuilder, JsonObjectBuilder> handler) {
    JsonObjectBuilder builder = new JsonObjectBuilder();
    handler.apply(builder);
    result.add(key, builder.build());
    return this;
  }

  public JsonObjectBuilder array(String key, Function<JsonArrayBuilder, JsonArrayBuilder> handler) {
    JsonArrayBuilder builder = new JsonArrayBuilder();
    handler.apply(builder);
    result.add(key, builder.build());
    return this;
  }

  public JsonObjectBuilder string(String key, String value) {
    result.addProperty(key, value);
    return this;
  }

  public JsonObjectBuilder bool(String key, boolean value) {
    result.addProperty(key, value);
    return this;
  }

  @Override
  public JsonElement build() {
    return this.result;
  }
}
