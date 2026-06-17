/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.constructor;

import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlainTextComponentConstructor implements ComponentConstructor<StringBuilder, String> {

  public static final PlainTextComponentConstructor INSTANCE = new PlainTextComponentConstructor();

  private PlainTextComponentConstructor() {}

  @Override
  public Class<String> getComponentClass() {
    return String.class;
  }

  @Override
  public boolean doesSupport(ConstructorFeature feature) {
    // Let's rather fail silently, because it should be obvious in applications of this specialized
    // component-constructor that only text is supported, without any click, hover or formatting to it.
    return true;
  }

  @Override
  public SlotContext getSlotContext(SlotType slot) {
    return SlotContext.getForSlot(slot);
  }

  @Override
  public StringBuilder createTextComponent(String text) {
    return new StringBuilder(text);
  }

  @Override
  public boolean setText(StringBuilder component, String text) {
    component.setLength(0);
    component.append(text);
    return true;
  }

  @Override
  public StringBuilder createKeyComponent(String key) {
    return new StringBuilder(key);
  }

  @Override
  public StringBuilder createTranslateComponent(String key, List<String> with, @Nullable String fallback) {
    return new StringBuilder(key);
  }

  @Override
  public void setClickChangePageAction(StringBuilder component, String value) {}

  @Override
  public void setClickCopyToClipboardAction(StringBuilder component, String value) {}

  @Override
  public void setClickOpenFileAction(StringBuilder component, String value) {}

  @Override
  public void setClickOpenUrlAction(StringBuilder component, String value) {}

  @Override
  public void setClickRunCommandAction(StringBuilder component, String value) {}

  @Override
  public void setClickSuggestCommandAction(StringBuilder component, String value) {}

  @Override
  public void setHoverItemAction(StringBuilder component, String material, @Nullable Integer count, @Nullable String name, @Nullable List<String> lore, boolean hideProperties) {}

  @Override
  public void setHoverTextAction(StringBuilder component, String text) {}

  @Override
  public void setHoverEntityAction(StringBuilder component, String type, UUID id, @Nullable String name) {}

  @Override
  public void setInsertAction(StringBuilder component, String value) {

  }

  @Override
  public void setColor(StringBuilder component, long packedColor, boolean allowOverwrite) {}

  @Override
  public void setShadowColor(StringBuilder component, long packedColor) {}

  @Override
  public void setFont(StringBuilder component, String font) {}

  @Override
  public void setObfuscatedFormat(StringBuilder component, TriState value) {}

  @Override
  public void setBoldFormat(StringBuilder component, TriState value) {}

  @Override
  public void setStrikethroughFormat(StringBuilder component, TriState value) {}

  @Override
  public void setUnderlinedFormat(StringBuilder component, TriState value) {}

  @Override
  public void setItalicFormat(StringBuilder component, TriState value) {}

  @Override
  public void addChildren(StringBuilder component, List<String> children) {
    children.forEach(component::append);
  }

  @Override
  public String finalizeComponent(StringBuilder component) {
    return component.toString();
  }

  @Override
  public void forEachTextOf(String component, Consumer<String> handler) {
    handler.accept(component);
  }

  @Override
  public void forEachNonTextUnitOf(String component, Consumer<String> handler) {
    // NO-OP, seeing how we turn everything into text.
  }
}
