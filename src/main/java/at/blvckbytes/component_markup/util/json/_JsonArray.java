package at.blvckbytes.component_markup.util.json;

import java.util.ArrayList;
import java.util.List;

public class _JsonArray implements _JsonElement {

  public final List<_JsonElement> items = new ArrayList<>();

  public _JsonArray add(_JsonElement value) {
    this.items.add(value);
    return this;
  }
}
