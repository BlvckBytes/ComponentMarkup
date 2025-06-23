package at.blvckbytes.component_markup.interpreter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class JsonObjectBuilder implements JsonBuilder {

  private final JsonObject result = new JsonObject();

  public JsonObjectBuilder object(String key, Consumer<JsonObjectBuilder> handler) {
    JsonObjectBuilder builder = new JsonObjectBuilder();
    handler.accept(builder);
    result.add(key, builder.build());
    return this;
  }

  public JsonObjectBuilder array(String key, Consumer<JsonArrayBuilder> handler) {
    JsonArrayBuilder builder = new JsonArrayBuilder();
    handler.accept(builder);
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
