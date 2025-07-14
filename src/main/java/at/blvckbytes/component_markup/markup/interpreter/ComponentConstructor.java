package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredRenderer;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface ComponentConstructor {

  // ================================================================================
  // SlotContext
  // ================================================================================

  SlotContext getSlotContext(SlotType slot);

  // ================================================================================
  // TerminalNode
  // ================================================================================

  Object createTextComponent(String text);

  Object createKeyComponent(String key);

  Object createTranslateComponent(String key, List<Object> with, @Nullable String fallback);

  DeferredComponent createDeferredComponent(
    DeferredRenderer<?> renderer,
    RendererParameter parameter,
    InterpretationEnvironment environmentSnapshot,
    SlotContext slotContext
  );

  // ================================================================================
  // Click-Action
  // ================================================================================

  void setClickChangePageAction(Object component, String value);

  void setClickCopyToClipboardAction(Object component, String value);

  void setClickOpenFileAction(Object component, String value);

  void setClickOpenUrlAction(Object component, URI value);

  void setClickRunCommandAction(Object component, String value);

  void setClickSuggestCommandAction(Object component, String value);

  // ================================================================================
  // Hover-Action
  // ================================================================================

  void setHoverItemAction(
    Object component,
    @Nullable String material,
    @Nullable Integer count,
    @Nullable Object name,
    @Nullable List<Object> lore,
    boolean hideProperties
  );

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

  void setColor(Object component, long packedColor);

  void setShadowColor(Object component, long packedColor);

  void setFont(Object component, @Nullable String font);

  void setObfuscatedFormat(Object component, TriState value);

  void setBoldFormat(Object component, TriState value);

  void setStrikethroughFormat(Object component, TriState value);

  void setUnderlinedFormat(Object component, TriState value);

  void setItalicFormat(Object component, TriState value);

  // ================================================================================
  // Miscellaneous
  // ================================================================================

  void setChildren(Object component, List<Object> children);

  @NotNull List<Object> getChildren(Object component);

  Object shallowCopy(Object component);

}
