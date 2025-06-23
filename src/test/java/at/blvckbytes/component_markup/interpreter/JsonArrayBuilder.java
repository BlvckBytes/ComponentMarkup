package at.blvckbytes.component_markup.interpreter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.function.Consumer;

public class JsonArrayBuilder implements JsonBuilder {

  private final JsonArray result = new JsonArray();

  public JsonArrayBuilder object(Consumer<JsonObjectBuilder> handler) {
    JsonObjectBuilder builder = new JsonObjectBuilder();
    handler.accept(builder);
    result.add(builder.build());
    return this;
  }

  public JsonArrayBuilder array(Consumer<JsonArrayBuilder> handler) {
    JsonArrayBuilder builder = new JsonArrayBuilder();
    handler.accept(builder);
    result.add(builder.build());
    return this;
  }

  @Override
  public JsonElement build() {
    return this.result;
  }
}
