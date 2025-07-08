package at.blvckbytes.component_markup.util.json;

import java.util.Map;
import java.util.TreeMap;

public class _JsonObject implements _JsonElement {

  public final Map<String, _JsonElement> entries = new TreeMap<>();

  public _JsonObject add(String key, _JsonElement value) {
    this.entries.put(key, value);
    return this;
  }
}
