/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ASTSubstitutionNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import at.blvckbytes.component_markup.markup.parser.MarkupParseError;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.parser.token.TokenEmitter;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    List<LetBinding> shorthandLetBindings = new ArrayList<>();

    ((InternalAttributeMap) attributes).forEachAttributeEntry((attributeName, attributeValues) -> {
      if (attributeValues.isEmpty())
        return;

      if (attributeValues.size() > 1)
        throw new MarkupParseException(attributeValues.get(1).attributeName.finalName, MarkupParseError.MULTIPLE_SAME_NAME_SHORTHAND_LET_BINDINGS);

      Attribute attributeValue = attributeValues.get(0);

      attributeValue.hasBeenUsed = true;

      if (attributeValue instanceof ExpressionAttribute) {
        shorthandLetBindings.add(new ExpressionLetBinding(
          ((ExpressionAttribute) attributeValue).value,
          attributeValue.attributeName.finalName, false
        ));

        return;
      }

      if (attributeValue instanceof MarkupAttribute) {
        shorthandLetBindings.add(new MarkupLetBinding(
          ((MarkupAttribute) attributeValue).value,
          attributeValue.attributeName.finalName, false
        ));

        return;
      }

      throw new IllegalStateException("Unaccounted-for attribute-type: " + attributeValue.getClass());
    });

    if (!shorthandLetBindings.isEmpty()) {
      if (letBindings == null)
        letBindings = new LinkedHashSet<>();

      letBindings.addAll(shorthandLetBindings);
    }

    return new ASTSubstitutionNode(
      MarkupParser.parseExpression(tagName.buildSubViewRelative(1), tokenEmitter),
      tagName, children, letBindings
    );
  }
}
