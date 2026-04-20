/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ASTSubstitutionNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.parser.MarkupParseError;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.parser.token.TokenEmitter;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ASTSubstitutionTag extends TagDefinition {

  public ASTSubstitutionTag() {
    super(TagClosing.INVARIANT, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.startsWith("$", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @Nullable TokenEmitter tokenEmitter,
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    if (tagName.length() == 1)
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_AST_SUBSTITUTION_EXPRESSION);

    if (tokenEmitter != null)
      tokenEmitter.emitCharToken(tagName.startInclusive, TokenType.MARKUP__OPERATOR__SUBSTITUTION);

    return new ASTSubstitutionNode(
      MarkupParser.parseExpression(tagName.buildSubViewRelative(1), tokenEmitter),
      tagName, children, letBindings
    );
  }
}
