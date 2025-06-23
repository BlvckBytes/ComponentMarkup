package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.style.Format;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.ast.tag.built_in.nbt.NbtSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsonComponentConstructor implements ComponentConstructor {

  // ================================================================================
  // ContentNode
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
  public Object createTranslateNode(String key, List<Object> with, @Nullable Object fallback) {
    JsonObject component = new JsonObject();
    component.addProperty("translate", key);

    if (!with.isEmpty()) {
      JsonArray withArray = new JsonArray();

      for (Object withItem : with)
        withArray.add((JsonObject) withItem);

      component.add("with", withArray);
    }

    if (fallback != null)
      component.add("fallback", (JsonObject) fallback);
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
  public Object createNbtNode(NbtSource source, String identifier, String path, boolean interpret, @Nullable Object separator) {
    JsonObject component = new JsonObject();

    component.addProperty("nbt", path);

    if (interpret)
      component.addProperty("interpret", true);

    if (separator != null)
      component.add("separator", (JsonObject) separator);

    switch (source) {
      case BLOCK:
        component.addProperty("block", identifier);
        break;
      case ENTITY:
        component.addProperty("entity", identifier);
        break;
      case STORAGE:
        component.addProperty("storage", identifier);
        break;

      default:
        return component;
    }

    return component;
  }

  // ================================================================================
  // Click-Action
  // ================================================================================

  @Override
  public void setClickAction(Object component, ClickAction action, String value) {
    JsonObject eventObject = new JsonObject();

    eventObject.addProperty("value", value);

    switch (action) {
      case OPEN_URL:
        eventObject.addProperty("action", "open_url");
        break;

      case OPEN_FILE:
        eventObject.addProperty("action", "open_file");
        break;

      case CHANGE_PAGE:
        eventObject.addProperty("action", "change_page");
        break;

      case RUN_COMMAND:
        eventObject.addProperty("action", "run_command");
        break;

      case SUGGEST_COMMAND:
        eventObject.addProperty("action", "suggest_command");
        break;

      case COPY_TO_CLIPBOARD:
        eventObject.addProperty("action", "copy_to_clipboard");
        break;
    }

    ((JsonObject) component).add("clickEvent", eventObject);
  }

  // ================================================================================
  // Hover-Action
  // ================================================================================

  @Override
  public void setHoverItemAction(Object component, String id, @Nullable Integer count, @Nullable Object name, @Nullable List<Object> lore) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    contentsObject.addProperty("id", id);

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
  public void setHoverEntityAction(Object component, String type, String id, @Nullable Object name) {
    JsonObject eventObject = new JsonObject();
    JsonObject contentsObject = new JsonObject();

    contentsObject.addProperty("type", type);
    contentsObject.addProperty("id", id);

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
  // Properties
  // ================================================================================

  @Override
  public void setStyle(Object component, NodeStyle style, boolean add, Interpreter interpreter) {
    if (style.color != null) {
      String color = interpreter.evaluateAsString(style.color);

      if (color != null)
        ((JsonObject) component).addProperty("color", color);
    }
    else if (!add)
      ((JsonObject) component).remove("color");

    if (style.font != null) {
      String font = interpreter.evaluateAsString(style.font);

      if (font != null)
        ((JsonObject) component).addProperty("font", font);
    }
    else if (!add)
      ((JsonObject) component).remove("font");

    for (Format format : Format.VALUES) {
      AExpression value = style.formatStates[format.ordinal()];
      String propertyName = format.name().toLowerCase();

      if (value == ImmediateExpression.ofNull()) {
        if (!add)
          ((JsonObject) component).remove(propertyName);

        continue;
      }

      Boolean state = interpreter.evaluateAsBoolean(value);

      if (state == null) {
        ((JsonObject) component).remove(propertyName);
        continue;
      }

      ((JsonObject) component).addProperty(propertyName, state);
    }
  }

  @Override
  public void setColor(Object component, @Nullable String color) {
    if (color == null) {
      ((JsonObject) component).remove("color");
      return;
    }

    ((JsonObject) component).addProperty("color", color);
  }

  @Override
  public void setChildren(Object component, List<Object> children) {
    JsonArray extra = new JsonArray();

    for (Object child : children)
      extra.add((JsonObject) child);

    ((JsonObject) component).add("extra", extra);
  }
}
