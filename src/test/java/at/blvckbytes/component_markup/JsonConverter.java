package at.blvckbytes.component_markup;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.util.json.*;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class JsonConverter {

  private static final Gson GSON_INSTANCE = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

  public static String jsonify(@Nullable Jsonifiable input) {
    if (input == null)
      return "null";

    return GSON_INSTANCE.toJson(toGoogleJson(input.jsonify()));
  }

  public static JsonElement toGoogleJson(_JsonElement internalJson) {
    if (internalJson == null)
      return JsonNull.INSTANCE;

    if (internalJson instanceof _JsonObject) {
      JsonObject result = new JsonObject();

      for (Map.Entry<String, _JsonElement> entry : ((_JsonObject) internalJson).entries.entrySet())
        result.add(entry.getKey(), toGoogleJson(entry.getValue()));

      return result;
    }

    if (internalJson instanceof _JsonArray) {
      JsonArray result = new JsonArray();

      for (_JsonElement item : ((_JsonArray) internalJson).items)
        result.add(toGoogleJson(item));

      return result;
    }

    if (internalJson instanceof _JsonString) {
      String value = ((_JsonString) internalJson).value;

      if (value == null)
        return JsonNull.INSTANCE;

      return new JsonPrimitive(value);
    }

    if (internalJson instanceof _JsonNumber) {
      Number value = ((_JsonNumber) internalJson).value;
      return new JsonPrimitive(value);
    }

    if (internalJson instanceof _JsonBoolean) {
      boolean value = ((_JsonBoolean) internalJson).value;
      return new JsonPrimitive(value);
    }

    throw new IllegalStateException("Unknown internal json-type: " + internalJson.getClass());
  }
}
