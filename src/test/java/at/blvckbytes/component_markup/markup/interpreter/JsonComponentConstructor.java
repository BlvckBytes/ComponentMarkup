package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.util.TriState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class JsonComponentConstructor implements ComponentConstructor {

  // ================================================================================
  // SlotContext
  // ================================================================================

  @Override
  public SlotContext getSlotContext(SlotType slot) {
    return SlotContext.getForSlot(slot);
  }

  // ================================================================================
  // TerminalNode
  // ================================================================================

  @Override
  public Object createTextNode(String text) {
    JsonObject component = new JsonObject();
    component.addProperty("text", text);
    return component;
  }

  @Override
  public Object createKeyNode(String key) {
    JsonObject component = new JsonObject();
    component.addProperty("keybind", key);
    return component;
  }

  @Override
  public Object createTranslateNode(String key, List<Object> with, @Nullable String fallback) {
    JsonObject component = new JsonObject();
    component.addProperty("translate", key);

    if (!with.isEmpty()) {
      JsonArray withArray = new JsonArray();

      for (Object withItem : with)
        withArray.add((JsonObject) withItem);

      component.add("with", withArray);
    }

    if (fallback != null)
      component.addProperty("fallback", fallback);

    return component;
  }

  @Override
  public Object createScoreNode(String name, String objective, @Nullable String value) {
    JsonObject component = new JsonObject();
    JsonObject scoreObject = new JsonObject();

    scoreObject.addProperty("name", name);
    scoreObject.addProperty("objective", objective);

    if (value != null)
      scoreObject.addProperty("value", value);

    component.add("score", scoreObject);

    return component;
  }

  @Override
  public Object createSelectorNode(String selector, @Nullable Object separator) {
    JsonObject component = new JsonObject();

    component.addProperty("selector", selector);

    if (separator != null)
      component.add("separator", (JsonObject) separator);

    return component;
  }

  @Override
  public Object createBlockNbtNode(String coordinates, String path, boolean interpret, @Nullable Object separator) {
    return createNbtNode("storage", coordinates, path, interpret, separator);
  }

  @Override
  public Object createEntityNbtNode(String selector, String path, boolean interpret, @Nullable Object separator) {
    return createNbtNode("entity", selector, path, interpret, separator);
  }

  @Override
  public Object createStorageNbtNode(String resource, String path, boolean interpret, @Nullable Object separator) {
    return createNbtNode("storage", resource, path, interpret, separator);
  }

  private Object createNbtNode(String sourceKey, String identifier, String path, boolean interpret, @Nullable Object separator) {
    JsonObject component = new JsonObject();

    component.addProperty("nbt", path);

    if (interpret)
      component.addProperty("interpret", true);

    if (separator != null)
      component.add("separator", (JsonObject) separator);

    component.addProperty(sourceKey, identifier);

    return component;
  }

  // ================================================================================
  // Click-Action
  // ================================================================================

  @Override
  public void setClickChangePageAction(Object component, int value) {
    setClickAction(component, "change_page", String.valueOf(value));
  }

  @Override
  public void setClickCopyToClipboardAction(Object component, String value) {
    setClickAction(component, "copy_to_clipboard", value);
  }

  @Override
  public void setClickOpenFileAction(Object component, String value) {
    setClickAction(component, "open_file", value);
  }

  @Override
  public void setClickOpenUrlAction(Object component, URI value) {
    setClickAction(component, "open_url", value.toString());
  }

  @Override
  public void setClickRunCommandAction(Object component, String value) {
    setClickAction(component, "run_command", value);
  }

  @Override
  public void setClickSuggestCommandAction(Object component, String value) {
    setClickAction(component, "suggest_command", value);
  }

  private void setClickAction(Object component, String action, String value) {
    JsonObject eventObject = new JsonObject();

    eventObject.addProperty("value", value);
    eventObject.addProperty("action", action);

    ((JsonObject) component).add("clickEvent", eventObject);
  }

  // ================================================================================
  // Hover-Action
  // ================================================================================

  @Override
  public void setHoverItemAction(Object component, @Nullable String material, @Nullable Integer count, @Nullable Object name, @Nullable List<Object> lore) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    if (material != null)
      contentsObject.addProperty("id", material);

    if (count != null)
      contentsObject.addProperty("count", count);

    if (name != null || lore != null) {
      JsonObject tagObject = new JsonObject();

      if (name != null)
        tagObject.add("name", (JsonObject) name);

      if (lore != null) {
        JsonArray loreArray = new JsonArray();

        for (Object loreItem : lore)
          loreArray.add((JsonElement) loreItem);

        tagObject.add("lore", loreArray);
      }

      contentsObject.add("tag", tagObject);
    }

    eventObject.addProperty("action", "show_item");
    eventObject.add("contents", contentsObject);

    ((JsonObject) component).add("hoverEvent", eventObject);
  }

  @Override
  public void setHoverTextAction(Object component, Object text) {
    JsonObject eventObject = new JsonObject();

    eventObject.addProperty("action", "show_text");
    eventObject.add("contents", (JsonObject) text);

    ((JsonObject) component).add("hoverEvent", eventObject);
  }

  @Override
  public void setHoverAchievementAction(Object component, String value) {
    JsonObject eventObject = new JsonObject();

    eventObject.addProperty("action", "show_achievement");
    eventObject.addProperty("contents", value);

    ((JsonObject) component).add("hoverEvent", eventObject);
  }

  @Override
  public void setHoverEntityAction(Object component, String type, UUID id, @Nullable Object name) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    contentsObject.addProperty("type", type);
    contentsObject.addProperty("id", id.toString());

    if (name != null)
      contentsObject.add("name", (JsonObject) name);

    eventObject.addProperty("action", "show_entity");
    eventObject.add("contents", contentsObject);

    ((JsonObject) component).add("hoverEvent", eventObject);
  }

  // ================================================================================
  // Insert-Action
  // ================================================================================

  @Override
  public void setInsertAction(Object component, String value) {
    ((JsonObject) component).addProperty("insertion", value);
  }

  // ================================================================================
  // Styling
  // ================================================================================

  @Override
  public void setColor(Object component, long packedColor) {
    if (packedColor == PackedColor.NULL_SENTINEL) {
      ((JsonObject) component).remove("color");
      return;
    }

    AnsiStyleColor ansiColor;

    if ((ansiColor = AnsiStyleColor.fromColor(packedColor)) != null) {
      ((JsonObject) component).addProperty("color", ansiColor.name);
      return;
    }

    ((JsonObject) component).addProperty("color", PackedColor.asNonAlphaHex(packedColor));
  }

  @Override
  public void setShadowColor(Object component, long packedColor) {
    if (packedColor == PackedColor.NULL_SENTINEL) {
      ((JsonObject) component).remove("shadow_color");
      return;
    }

    AnsiStyleColor ansiColor;

    if ((ansiColor = AnsiStyleColor.fromColor(packedColor)) != null) {
      ((JsonObject) component).addProperty("shadow_color", ansiColor.name);
      return;
    }

    ((JsonObject) component).addProperty("shadow_color", PackedColor.asNonAlphaHex(packedColor));
  }

  @Override
  public void setFont(Object component, @Nullable String font) {
    if (font == null) {
      ((JsonObject) component).remove("font");
      return;
    }

    ((JsonObject) component).addProperty("font", font);
  }

  @Override
  public void setObfuscatedFormat(Object component, TriState value) {
    setFormat(component, "obfuscated", value);
  }

  @Override
  public void setBoldFormat(Object component, TriState value) {
    setFormat(component, "bold", value);
  }

  @Override
  public void setStrikethroughFormat(Object component, TriState value) {
    setFormat(component, "strikethrough", value);
  }

  @Override
  public void setUnderlinedFormat(Object component, TriState value) {
    setFormat(component, "underlined", value);
  }

  @Override
  public void setItalicFormat(Object component, TriState value) {
    setFormat(component, "italic", value);
  }

  private void setFormat(Object component, String formatKey, TriState value) {
    if (value == TriState.NULL) {
      ((JsonObject) component).remove(formatKey);
      return;
    }

    ((JsonObject) component).addProperty(formatKey, value == TriState.TRUE);
  }

  // ================================================================================
  // Miscellaneous
  // ================================================================================

  @Override
  public void setChildren(Object component, List<Object> children) {
    JsonArray extra = new JsonArray();

    for (Object child : children)
      extra.add((JsonObject) child);

    ((JsonObject) component).add("extra", extra);
  }
}
