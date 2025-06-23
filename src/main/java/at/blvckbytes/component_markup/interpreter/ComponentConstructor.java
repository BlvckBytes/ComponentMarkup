package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.ast.tag.built_in.nbt.NbtSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ComponentConstructor {

  // ================================================================================
  // ContentNode
  // ================================================================================

  Object createTextNode(String text);

  Object createKeyNode(String key);

  Object createTranslateNode(String key, List<Object> with, @Nullable Object fallback);

  Object createScoreNode(String name, String objective, @Nullable String value);

  Object createSelectorNode(String selector, @Nullable Object separator);

  Object createNbtNode(NbtSource source, String identifier, String path, boolean interpret, @Nullable Object separator);

  // ================================================================================
  // Click-Action
  // ================================================================================

  void setClickAction(Object component, ClickAction action, String value);

  // ================================================================================
  // Hover-Action
  // ================================================================================

  void setHoverItemAction(Object component, String id, @Nullable Integer count, @Nullable Object name, @Nullable List<Object> lore);

  void setHoverTextAction(Object component, Object text);

  void setHoverAchievementAction(Object component, String value);

  void setHoverEntityAction(Object component, String type, String id, @Nullable Object name);

  // ================================================================================
  // Insert-Action
  // ================================================================================

  void setInsertAction(Object component, String value);

  // ================================================================================
  // Properties
  // ================================================================================

  void setStyle(Object component, NodeStyle style, boolean add, Interpreter interpreter);

  void setColor(Object component, @Nullable String color);

  void setChildren(Object component, List<Object> children);

}
