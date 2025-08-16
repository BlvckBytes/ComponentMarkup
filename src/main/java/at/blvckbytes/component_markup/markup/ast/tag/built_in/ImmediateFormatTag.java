/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ImmediateFormatTag extends TagDefinition {

  public ImmediateFormatTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return applyFormat(tagName, null);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(tagName, children, letBindings);
    applyFormat(tagName, wrapper.getOrInstantiateStyle());
    return wrapper;
  }

  private boolean applyFormat(InputView tagName, @Nullable NodeStyle style) {
    int tagNameLength = tagName.length();

    if (tagNameLength == 0)
      return false;

    char firstChar = tagName.nthChar(0);

    boolean isNegative = (
      firstChar == '!'
        || (firstChar == '&' && tagNameLength > 1 && tagName.nthChar(1) == '!')
    );

    Format format;

    switch (tagName.buildString()) {
      case "&l":
      case "&!l":
      case "b":
      case "bold":
      case "!b":
      case "!bold":
        format = Format.BOLD;
        break;

      case "&o":
      case "&!o":
      case "i":
      case "italic":
      case "!i":
      case "!italic":
        format = Format.ITALIC;
        break;

      case "&n":
      case "&!n":
      case "u":
      case "underlined":
      case "!u":
      case "!underlined":
        format = Format.UNDERLINED;
        break;

      case "&m":
      case "&!m":
      case "st":
      case "strikethrough":
      case "!st":
      case "!strikethrough":
        format = Format.STRIKETHROUGH;
        break;

      case "&k":
      case "&!k":
      case "obf":
      case "obfuscated":
      case "!obf":
      case "!obfuscated":
        format = Format.OBFUSCATED;
        break;

      default:
        return false;
    }

    if (style != null)
      style.setFormat(format, ImmediateExpression.ofBoolean(tagName, !isNegative));

    return true;
  }
}
