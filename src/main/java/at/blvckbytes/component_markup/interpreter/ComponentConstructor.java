package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.style.Format;
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

  Object createBlockNbtNode(String coordinates, String path, boolean interpret, @Nullable Object separator);

  Object createEntityNbtNode(String selector, String path, boolean interpret, @Nullable Object separator);

  Object createStorageNbtNode(String resource, String path, boolean interpret, @Nullable Object separator);

  // ================================================================================
  // Click-Action
  // ================================================================================

  void setClickChangePageAction(Object component, String value);

  void setClickCopyToClipboardAction(Object component, String value);

  void setClickOpenFileAction(Object component, String value);

  void setClickOpenUrlAction(Object component, String value);

  void setClickRunCommandAction(Object component, String value);

  void setClickSuggestCommandAction(Object component, String value);

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
  // Styling
  // ================================================================================

  void setColor(Object component, @Nullable String color);

  void setFont(Object component, @Nullable String font);

  void setFormat(Object component, Format format, @Nullable Boolean value);

  // ================================================================================
  // Miscellaneous
  // ================================================================================

  void setChildren(Object component, List<Object> children);

}
