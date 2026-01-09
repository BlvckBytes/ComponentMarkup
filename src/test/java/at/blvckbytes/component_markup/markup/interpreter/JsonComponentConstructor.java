/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.constructor.*;
import at.blvckbytes.component_markup.util.TriState;
import at.blvckbytes.component_markup.util.color.AnsiStyleColor;
import at.blvckbytes.component_markup.util.color.PackedColor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class JsonComponentConstructor implements ComponentConstructor<JsonObject, JsonObject> {

  @Override
  public Class<JsonObject> getComponentClass() {
    return JsonObject.class;
  }

  @Override
  public boolean doesSupport(ConstructorFeature feature) {
    return true;
  }

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
  public JsonObject createTextComponent(String text) {
    JsonObject component = new JsonObject();
    component.addProperty("text", text);
    return component;
  }

  @Override
  public JsonObject createKeyComponent(String key) {
    JsonObject component = new JsonObject();
    component.addProperty("keybind", key);
    return component;
  }

  @Override
  public JsonObject createTranslateComponent(String key, List<JsonObject> with, @Nullable String fallback) {
    JsonObject component = new JsonObject();
    component.addProperty("translate", key);

    if (!with.isEmpty()) {
      JsonArray withArray = new JsonArray();

      for (JsonObject withItem : with)
        withArray.add(withItem);

      component.add("with", withArray);
    }

    if (fallback != null)
      component.addProperty("fallback", fallback);

    return component;
  }

  // ================================================================================
  // Click-Action
  // ================================================================================

  @Override
  public void setClickChangePageAction(JsonObject component, String value) {
    setClickAction(component, "change_page", value);
  }

  @Override
  public void setClickCopyToClipboardAction(JsonObject component, String value) {
    setClickAction(component, "copy_to_clipboard", value);
  }

  @Override
  public void setClickOpenFileAction(JsonObject component, String value) {
    setClickAction(component, "open_file", value);
  }

  @Override
  public void setClickOpenUrlAction(JsonObject component, String value) {
    setClickAction(component, "open_url", value);
  }

  @Override
  public void setClickRunCommandAction(JsonObject component, String value) {
    setClickAction(component, "run_command", value);
  }

  @Override
  public void setClickSuggestCommandAction(JsonObject component, String value) {
    setClickAction(component, "suggest_command", value);
  }

  private void setClickAction(JsonObject component, String action, String value) {
    JsonObject eventObject = new JsonObject();

    eventObject.addProperty("value", value);
    eventObject.addProperty("action", action);

    component.add("clickEvent", eventObject);
  }

  // ================================================================================
  // Hover-Action
  // ================================================================================

  @Override
  public void setHoverItemAction(
    JsonObject component,
    String material,
    @Nullable Integer count,
    @Nullable JsonObject name,
    @Nullable List<JsonObject> lore,
    boolean hideFlags
  ) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    contentsObject.addProperty("id", material);

    if (count != null)
      contentsObject.addProperty("count", count);

    contentsObject.addProperty("hideFlags", hideFlags);

    if (name != null || lore != null) {
      JsonObject tagObject = new JsonObject();

      if (name != null)
        tagObject.add("name", name);

      if (lore != null) {
        JsonArray loreArray = new JsonArray();

        for (JsonObject loreItem : lore)
          loreArray.add(loreItem);

        tagObject.add("lore", loreArray);
      }

      contentsObject.add("tag", tagObject);
    }

    eventObject.addProperty("action", "show_item");
    eventObject.add("contents", contentsObject);

    component.add("hoverEvent", eventObject);
  }

  @Override
  public void setHoverTextAction(JsonObject component, JsonObject text) {
    JsonObject eventObject = new JsonObject();

    eventObject.addProperty("action", "show_text");
    eventObject.add("contents", text);

    component.add("hoverEvent", eventObject);
  }

  @Override
  public void setHoverEntityAction(JsonObject component, String type, UUID id, @Nullable JsonObject name) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    contentsObject.addProperty("type", type);
    contentsObject.addProperty("id", id.toString());

    if (name != null)
      contentsObject.add("name", name);

    eventObject.addProperty("action", "show_entity");
    eventObject.add("contents", contentsObject);

    component.add("hoverEvent", eventObject);
  }

  // ================================================================================
  // Insert-Action
  // ================================================================================

  @Override
  public void setInsertAction(JsonObject component, String value) {
    component.addProperty("insertion", value);
  }

  // ================================================================================
  // Styling
  // ================================================================================

  @Override
  public void setColor(JsonObject component, long packedColor) {
    if (packedColor == PackedColor.NULL_SENTINEL) {
      component.remove("color");
      return;
    }

    AnsiStyleColor ansiColor;

    if ((ansiColor = AnsiStyleColor.fromColor(packedColor)) != null) {
      component.addProperty("color", ansiColor.name);
      return;
    }

    component.addProperty("color", PackedColor.asNonAlphaHex(packedColor));
  }

  @Override
  public void setShadowColor(JsonObject component, long packedColor) {
    if (packedColor == PackedColor.NULL_SENTINEL) {
      component.remove("shadow_color");
      return;
    }

    AnsiStyleColor ansiColor;

    if ((ansiColor = AnsiStyleColor.fromColor(packedColor)) != null) {
      component.addProperty("shadow_color", ansiColor.name);
      return;
    }

    component.addProperty("shadow_color", PackedColor.asAlphaHex(packedColor));
  }

  @Override
  public void setFont(JsonObject component, String font) {
    component.addProperty("font", font);
  }

  @Override
  public void setObfuscatedFormat(JsonObject component, TriState value) {
    setFormat(component, "obfuscated", value);
  }

  @Override
  public void setBoldFormat(JsonObject component, TriState value) {
    setFormat(component, "bold", value);
  }

  @Override
  public void setStrikethroughFormat(JsonObject component, TriState value) {
    setFormat(component, "strikethrough", value);
  }

  @Override
  public void setUnderlinedFormat(JsonObject component, TriState value) {
    setFormat(component, "underlined", value);
  }

  @Override
  public void setItalicFormat(JsonObject component, TriState value) {
    setFormat(component, "italic", value);
  }

  @Override
  public JsonObject finalizeComponent(JsonObject component) {
    // Breaking the reference at this point is of utmost importance, because the exact same will
    // happen on a real Paper server, where the builder is built - all changes to the builder beyond
    // this point have no effect on the resulting component, and this reflects that behavior.
    return component.deepCopy();
  }

  private void setFormat(JsonObject component, String formatKey, TriState value) {
    if (value == TriState.NULL) {
      component.remove(formatKey);
      return;
    }

    component.addProperty(formatKey, value == TriState.TRUE);
  }

  // ================================================================================
  // Children
  // ================================================================================

  @Override
  public void addChildren(JsonObject component, List<JsonObject> children) {
    JsonElement existingChildren = component.get("extra");
    JsonArray extra = existingChildren == null ? new JsonArray() : (JsonArray) existingChildren;

    for (JsonObject child : children)
      extra.add(child);

    component.add("extra", extra);
  }
}
