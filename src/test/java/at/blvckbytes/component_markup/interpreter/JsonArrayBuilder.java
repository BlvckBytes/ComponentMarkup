package at.blvckbytes.component_markup.interpreter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class JsonArrayBuilder implements JsonBuilder {

  private final JsonArray result = new JsonArray();

  public JsonArrayBuilder item(JsonObjectBuilder builder) {
    result.add(builder.build());
    return this;
  }

  public JsonArrayBuilder item(JsonArrayBuilder builder) {
    result.add(builder.build());
    return this;
  }

  @Override
  public JsonElement build() {
    return this.result;
  }
}
