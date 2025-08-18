/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredRenderer;
import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import at.blvckbytes.component_markup.platform.*;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.TriState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class JsonComponentConstructor implements ComponentConstructor {

  private static class DeferredElement extends JsonElement implements DeferredComponent {

    @Override
    public JsonElement deepCopy() {
      throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable List<Object> renderDeferredComponent(@Nullable PlatformEntity recipient) {
      throw new UnsupportedOperationException();
    }
  }

  // ================================================================================
  // SlotContext
  // ================================================================================

  @Override
  public boolean doesSupport(PlatformFeature feature) {
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

  @Override
  public DeferredComponent createDeferredComponent(
    DeferredRenderer<?> renderer,
    RendererParameter parameter,
    InterpretationEnvironment environmentSnapshot,
    SlotContext slotContext
  ) {
    // TODO: Implement
    return new DeferredElement();
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
    @Nullable String material,
    @Nullable Integer count,
    @Nullable Object name,
    @Nullable List<Object> lore,
    boolean hideFlags
  ) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    if (material != null)
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

  private JsonArray toJsonArray(@Nullable List<Object> children) {
    JsonArray result = new JsonArray();

    if (children != null) {
      for (Object child : children)
        result.add((JsonElement) child);
    }

    return result;
  }

  @Override
  public @Nullable Object setMembers(Object component, MembersSlot slot, @Nullable List<Object> children) {
    JsonPropertyRW propertyRW = accessProperty((JsonObject) component, slot);

    if (propertyRW == null)
      return null;

    switch (slot) {
      case HOVER_ITEM_NAME:
      case HOVER_ENTITY_NAME:
      case HOVER_TEXT_VALUE: {
        if (children == null) {
          if (!propertyRW.write(null))
            return null;

          return component;
        }

        if (children.size() > 1)
          LoggerProvider.log(Level.WARNING, "Expected children for slot " + slot + " to be a singleton-list");

        if (!propertyRW.write((JsonElement) children.get(0)))
          return null;

        return component;
      }

      default:
        if (!propertyRW.write(toJsonArray(children)))
          return null;

        return component;
    }
  }

  @Override
  public @Nullable List<Object> getMembers(Object component, MembersSlot slot) {
    JsonPropertyRW propertyRW = accessProperty((JsonObject) component, slot);

    if (propertyRW == null)
      return null;

    JsonElement value = propertyRW.read();

    if (value == null)
      return null;

    List<Object> result = new ArrayList<>();

    if (value instanceof JsonArray) {
      for (JsonElement item : (JsonArray) value)
        result.add(item);

      return result;
    }

    result.add(value);

    return result;
  }

  private static class JsonPropertyRW {

    final Supplier<JsonElement> _read;
    final Consumer<JsonElement> _write;

    JsonPropertyRW(Supplier<JsonElement> read, Consumer<JsonElement> write) {
      _read = read;
      _write = write;
    }

    boolean write(JsonElement element) {
      try {
        this._write.accept(element);
        return true;
      } catch (ClassCastException | NullPointerException ignored) {
        return false;
      }
    }

    @Nullable JsonElement read() {
      try {
        return this._read.get();
      } catch (ClassCastException | NullPointerException ignored) {
        return null;
      }
    }
  }

  private JsonPropertyRW accessProperty(JsonObject component, MembersSlot slot) {
    switch (slot) {
      case CHILDREN: {
        return new JsonPropertyRW(
          () -> component.getAsJsonArray("extra"),
          value -> component.add("extra", value)
        );
      }

      case TRANSLATE_WITH: {
        if (!component.has("translate"))
          return null;

        return new JsonPropertyRW(
          () -> component.getAsJsonArray("with"),
          value -> component.add("with", value)
        );
      }

      case HOVER_ENTITY_NAME:
      case HOVER_ITEM_LORE:
      case HOVER_ITEM_NAME:
      case HOVER_TEXT_VALUE: {
        JsonObject eventObject = component.getAsJsonObject("hoverEvent");
        String action = eventObject.get("action").getAsString();

        switch (slot) {
          case HOVER_ENTITY_NAME: {
            if (!action.equals("show_entity"))
              return null;

            return new JsonPropertyRW(
              () -> eventObject.get("name"),
              value -> eventObject.add("name", value)
            );
          }

          case HOVER_ITEM_LORE:
          case HOVER_ITEM_NAME: {
            if (!action.equals("show_item"))
              return null;

            JsonObject tag = eventObject.getAsJsonObject("contents").getAsJsonObject("tag");

            if (slot == MembersSlot.HOVER_ITEM_NAME) {
              JsonElement nameElement = tag.get("name");

              return new JsonPropertyRW(
                () -> tag.get("name"),
                value -> tag.add("name", value)
              );
            }

            return new JsonPropertyRW(
              () -> tag.get("lore"),
              value -> tag.add("lore", value)
            );
          }

          case HOVER_TEXT_VALUE: {
            if (!action.equals("show_text"))
              return null;

            return new JsonPropertyRW(
              () -> component.get("contents"),
              value -> component.add("contents", value)
            );
          }
        }
      }
    }

    return null;
  }

  @Override
  public Object shallowCopyIncludingMemberLists(Object component) {
    JsonObject copy = new JsonObject();
    JsonObject source = (JsonObject) component;

    for (String key : source.keySet()) {
      JsonElement sourceValue = source.get(key);

      if (sourceValue instanceof JsonArray)
        sourceValue = copyArray((JsonArray) sourceValue);

      copy.add(key, sourceValue);
    }

    return copy;
  }

  private JsonArray copyArray(JsonArray input) {
    JsonArray result = new JsonArray();

    for (JsonElement item : input)
      result.add(item);

    return result;
  }
}
