package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface ComponentConstructor {

  // ================================================================================
  // TerminalNode
  // ================================================================================

  Object createTextNode(String text);

  Object createKeyNode(String key);

  Object createTranslateNode(String key, List<Object> with, @Nullable String fallback);

  Object createScoreNode(String name, String objective, @Nullable String value);

  Object createSelectorNode(String selector, @Nullable Object separator);

  Object createBlockNbtNode(String coordinates, String path, boolean interpret, @Nullable Object separator);

  Object createEntityNbtNode(String selector, String path, boolean interpret, @Nullable Object separator);

  Object createStorageNbtNode(String resource, String path, boolean interpret, @Nullable Object separator);

  // ================================================================================
  // Click-Action
  // ================================================================================

  void setClickChangePageAction(Object component, int value);

  void setClickCopyToClipboardAction(Object component, String value);

  void setClickOpenFileAction(Object component, String value);

  void setClickOpenUrlAction(Object component, URI value);

  void setClickRunCommandAction(Object component, String value);

  void setClickSuggestCommandAction(Object component, String value);

  // ================================================================================
  // Hover-Action
  // ================================================================================

  void setHoverItemAction(Object component, @Nullable String material, @Nullable Integer count, @Nullable Object name, @Nullable List<Object> lore);

  void setHoverTextAction(Object component, Object text);

  void setHoverAchievementAction(Object component, String value);

  void setHoverEntityAction(Object component, String type, UUID id, @Nullable Object name);

  // ================================================================================
  // Insert-Action
  // ================================================================================

  void setInsertAction(Object component, String value);

  // ================================================================================
  // Styling
  // ================================================================================

  void setColor(Object component, int packedColor);

  void setShadowColor(Object component, int packedColor);

  void setFont(Object component, @Nullable String font);

  void setObfuscatedFormat(Object component, @Nullable Boolean value);

  void setBoldFormat(Object component, @Nullable Boolean value);

  void setStrikethroughFormat(Object component, @Nullable Boolean value);

  void setUnderlinedFormat(Object component, @Nullable Boolean value);

  void setItalicFormat(Object component, @Nullable Boolean value);

  // ================================================================================
  // Miscellaneous
  // ================================================================================

  void setChildren(Object component, List<Object> children);

}
