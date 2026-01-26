/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.parser.AttributeFlag;
import at.blvckbytes.component_markup.markup.parser.MarkupParseError;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InternalAttributeMap implements AttributeMap {

  private final InputView tagName;

  private final Map<String, List<Attribute>> attributeMap;

  public InternalAttributeMap(InputView tagName) {
    this.attributeMap = new HashMap<>();
    this.tagName = tagName;
  }

  public void add(Attribute attribute) {
    this.attributeMap
      .computeIfAbsent(getPossiblyTransformedAttributeName(attribute), k -> new ArrayList<>())
      .add(attribute);
  }

  public void validateNoUnusedAttributes() {
    for (List<Attribute> attributeBucket : attributeMap.values()) {
      for (Attribute attribute : attributeBucket) {
        if (!attribute.hasBeenUsed)
          throw new MarkupParseException(attribute.attributeName.finalName, MarkupParseError.UNSUPPORTED_ATTRIBUTE, tagName.buildString(), getPossiblyTransformedAttributeName(attribute));
      }
    }
  }

  public void markAllUsed() {
    for (List<Attribute> attributeBucket : attributeMap.values()) {
      for (Attribute attribute : attributeBucket)
        attribute.hasBeenUsed = true;
    }
  }

  // ================================================================================
  // NON_MULTI
  // ================================================================================

  @Override
  public @NotNull ExpressionNode getMandatoryExpressionNode(String name, String... aliases) {
    ExpressionNode result = getOptionalExpressionNode(name, aliases);

    if (result == null)
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagName.buildString(), formatNames(name, aliases));

    return result;
  }

  @Override
  public @Nullable ExpressionNode getOptionalExpressionNode(String name, String... aliases) {
    Attribute attribute = selectNonMultiAttributeOrNull(name, aliases);

    if (attribute == null)
      return null;

    if (!(attribute instanceof ExpressionAttribute))
      throw new MarkupParseException(attribute.attributeName.finalName, MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE, formatNames(name, aliases), tagName.buildString());

    ExpressionAttribute expressionAttribute = (ExpressionAttribute) attribute;

    if (expressionAttribute.attributeName.has(AttributeFlag.SPREAD_MODE))
      throw new MarkupParseException(attribute.attributeName.finalName, MarkupParseError.SPREAD_ON_NON_MULTI_ATTRIBUTE, formatNames(name, aliases), tagName.buildString());

    return expressionAttribute.value;
  }

  @Override
  public @NotNull MarkupNode getMandatoryMarkupNode(String name, String... aliases) {
    MarkupNode result = getOptionalMarkupNode(name, aliases);

    if (result == null)
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagName.buildString(), formatNames(name, aliases));

    return result;
  }

  @Override
  public @Nullable MarkupNode getOptionalMarkupNode(String name, String... aliases) {
    Attribute attribute = selectNonMultiAttributeOrNull(name, aliases);

    if (attribute == null)
      return null;

    return attribute.asMarkupNode();
  }

  private @Nullable Attribute selectNonMultiAttributeOrNull(String name, String... aliases) {
    List<Attribute> attributes = getAttributes(name, aliases);

    if (attributes == null)
      return null;

    int attributesCount = attributes.size();

    if (attributesCount == 0)
      return null;

    if (attributesCount > 1)
      throw new MarkupParseException(attributes.get(1).attributeName.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, formatNames(name, aliases), tagName.buildString());

    Attribute result = attributes.get(0);

    result.hasBeenUsed = true;

    return result;
  }

  // ================================================================================
  // MULTI
  // ================================================================================

  @Override
  public @NotNull ExpressionList getMandatoryExpressionList(String name, String... aliases) {
    ExpressionList result = unwrapExpressionAttributes(selectMultiAttributeOrNull(name, true, aliases));

    if (result.isEmpty())
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagName.buildString(), formatNames(name, aliases));

    return result;
  }

  @Override
  public @NotNull ExpressionList getOptionalExpressionList(String name, String... aliases) {
    return unwrapExpressionAttributes(selectMultiAttributeOrNull(name, true, aliases));
  }

  @Override
  public @NotNull MarkupList getMandatoryMarkupList(String name, String... aliases) {
    MarkupList result = unwrapMarkupAttributes(selectMultiAttributeOrNull(name, false, aliases));

    if (result.isEmpty())
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagName.buildString(), formatNames(name, aliases));

    return result;
  }

  @Override
  public @NotNull MarkupList getOptionalMarkupList(String name, String... aliases) {
    return unwrapMarkupAttributes(selectMultiAttributeOrNull(name, false, aliases));
  }

  @Override
  public @NotNull MarkupList getRemainingValuesInOrderAsMarkup() {
    List<Attribute> remainingValues = new ArrayList<>();

    for (List<Attribute> bucket : attributeMap.values()) {
      for (Attribute attribute : bucket) {
        if (attribute.hasBeenUsed)
          continue;

        attribute.hasBeenUsed = true;
        remainingValues.add(attribute);
      }
    }

    remainingValues.sort(Comparator.comparingInt(attribute -> attribute.attributeName.fullName.startInclusive));

    return new MarkupList(remainingValues);
  }

  @Override
  public boolean hasUnusedValues() {
    for (List<Attribute> bucket : attributeMap.values()) {
      for (Attribute attribute : bucket) {
        if (!attribute.hasBeenUsed)
          return true;
      }
    }

    return false;
  }

  private @Nullable List<Attribute> selectMultiAttributeOrNull(String name, boolean expression, String... aliases) {
    List<Attribute> attributes = getAttributes(name, aliases);

    if (attributes == null)
      return null;

    for (Attribute attribute : attributes) {
      if (expression) {
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(attribute.attributeName.finalName, MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE, formatNames(name, aliases), tagName.buildString());

        attribute.hasBeenUsed = true;
        continue;
      }

      attribute.hasBeenUsed = true;
    }

    return attributes;
  }

  private MarkupList unwrapMarkupAttributes(@Nullable List<Attribute> attributes) {
    if (attributes == null)
      return MarkupList.EMPTY;

    return new MarkupList(attributes);
  }

  private ExpressionList unwrapExpressionAttributes(@Nullable List<Attribute> attributes) {
    if (attributes == null)
      return ExpressionList.EMPTY;

    ExpressionList result = new ExpressionList(attributes.size());

    for (Attribute attribute : attributes)
      result.add((ExpressionAttribute) attribute);

    return result;
  }

  private String formatNames(String name, String... aliases) {
    if (aliases.length == 0)
      return name;

    StringBuilder result = new StringBuilder();

    result.append(name);

    result.append(" (alias").append(aliases.length > 1 ? "es" : "").append(": ");

    for (int aliasIndex = 0; aliasIndex < aliases.length; ++aliasIndex) {
      if (aliasIndex != 0)
        result.append(", ");

      result.append(aliases[aliasIndex]);
    }

    result.append(')');

    return result.toString();
  }

  private @Nullable List<Attribute> getAttributes(String name, String... aliases) {
    if (aliases.length == 0)
      return attributeMap.get(name);

    List<Attribute> result = null;
    boolean isResultDirectRef = false;

    List<Attribute> selection;

    if ((selection = attributeMap.get(name)) != null) {
      result = selection;
      isResultDirectRef = true;
    }

    for (String alias : aliases) {
      if ((selection = attributeMap.get(alias)) != null) {
        if (isResultDirectRef)
          result = new ArrayList<>(result);

        else if (result == null)
          result = new ArrayList<>();

        result.addAll(selection);
      }
    }

    return result;
  }

  private String getPossiblyTransformedAttributeName(Attribute attribute) {
    String name = attribute.attributeName.finalName.buildString();

    if (attribute.attributeName.has(AttributeFlag.BIND_BY_NAME))
      return name.replace('_', '-');

    return name;
  }
}
