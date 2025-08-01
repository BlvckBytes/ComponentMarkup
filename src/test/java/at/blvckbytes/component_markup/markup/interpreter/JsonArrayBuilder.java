/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.function.Function;

public class JsonArrayBuilder implements JsonBuilder {

  private final JsonArray result = new JsonArray();

  public JsonArrayBuilder object(Function<JsonObjectBuilder, JsonObjectBuilder> handler) {
    JsonObjectBuilder builder = new JsonObjectBuilder();
    result.add(handler.apply(builder).build());
    return this;
  }

  public JsonArrayBuilder array(Function<JsonArrayBuilder, JsonArrayBuilder> handler) {
    JsonArrayBuilder builder = new JsonArrayBuilder();
    result.add(handler.apply(builder).build());
    return this;
  }

  public JsonArrayBuilder reverse(boolean reverse) {
    if (!reverse)
      return this;

    int resultSize = result.size();

    for (int index = 0; index < resultSize / 2; ++index) {
      JsonElement headElement = result.get(index);

      int tailIndex = resultSize - 1 - index;
      JsonElement tailElement = result.get(tailIndex);

      result.set(index, tailElement);
      result.set(tailIndex, headElement);
    }

    return this;
  }

  @Override
  public JsonElement build() {
    return this.result;
  }
}
