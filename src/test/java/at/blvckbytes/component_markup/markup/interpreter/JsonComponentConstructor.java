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

public class JsonComponentConstructor implements ComponentConstructor {

  // ================================================================================
  // SlotContext
  // ================================================================================

  @Override
  public boolean doesSupport(ConstructorFeature feature) {
    return true;
  }

  @Override
  public SlotContext getSlotContext(SlotType slot) {
    return SlotContext.getForSlot(slot);
  }

  // ================================================================================
  // TerminalNode
  // ================================================================================

  @Override
  public Object createTextComponent(String text) {
    JsonObject component = new JsonObject();
    component.addProperty("text", text);
    return component;
  }

  @Override
  public Object createKeyComponent(String key) {
    JsonObject component = new JsonObject();
    component.addProperty("keybind", key);
    return component;
  }

  @Override
  public Object createTranslateComponent(String key, List<Object> with, @Nullable String fallback) {
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

  // ================================================================================
  // Click-Action
  // ================================================================================

  @Override
  public void setClickChangePageAction(Object component, String value) {
    setClickAction(component, "change_page", value);
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
  public void setClickOpenUrlAction(Object component, String value) {
    setClickAction(component, "open_url", value);
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
  public void setHoverItemAction(
    Object component,
    String material,
    @Nullable Integer count,
    @Nullable Object name,
    @Nullable List<Object> lore,
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

    ((JsonObject) component).addProperty("shadow_color", PackedColor.asAlphaHex(packedColor));
  }

  @Override
  public void setFont(Object component, String font) {
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

  @Override
  public Object finaliseComponent(Object component) {
    return component;
  }

  private void setFormat(Object component, String formatKey, TriState value) {
    if (value == TriState.NULL) {
      ((JsonObject) component).remove(formatKey);
      return;
    }

    ((JsonObject) component).addProperty(formatKey, value == TriState.TRUE);
  }

  // ================================================================================
  // Children
  // ================================================================================

  @Override
  public void setChildren(Object component, List<Object> children) {
    if (children == null) {
      ((JsonObject) component).remove("extra");
      return;
    }

    JsonArray extra = new JsonArray();

    for (Object child : children)
      extra.add((JsonObject) child);

    ((JsonObject) component).add("extra", extra);
  }
}
