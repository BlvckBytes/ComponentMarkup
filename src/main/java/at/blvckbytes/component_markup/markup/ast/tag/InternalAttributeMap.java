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

    if (expressionAttribute.attributeName.flags.contains(AttributeFlag.SPREAD_MODE))
      throw new MarkupParseException(attribute.attributeName.finalName, MarkupParseError.SPREAD_ON_NON_MULTI_ATTRIBUTE, formatNames(name, aliases), tagName.buildString());

    return expressionAttribute.value;
  }

  @Override
  public @Nullable ExpressionNode getOptionalBoundFlagExpressionNode() {
    List<ExpressionAttribute> attributes = selectUnusedBoundFlagAttributesInOrder();

    if (attributes.isEmpty())
      return null;

    if (attributes.size() > 1)
      throw new MarkupParseException(attributes.get(1).attributeName.fullName, MarkupParseError.MULTIPLE_BOUND_FLAG_ATTRIBUTES, tagName.buildString());

    return attributes.get(0).value;
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

  @Override
  public @Nullable MarkupNode getOptionalBoundFlagMarkupNode() {
    List<ExpressionAttribute> attributes = selectUnusedBoundFlagAttributesInOrder();

    if (attributes.isEmpty())
      return null;

    if (attributes.size() > 1)
      throw new MarkupParseException(attributes.get(1).attributeName.fullName, MarkupParseError.MULTIPLE_BOUND_FLAG_ATTRIBUTES, tagName.buildString());

    return attributes.get(0).asMarkupNode();
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
    ExpressionList result = getOptionalExpressionList(name, aliases);

    if (result.isEmpty())
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagName.buildString(), formatNames(name, aliases));

    return result;
  }

  @Override
  public @NotNull ExpressionList getOptionalExpressionList(String name, String... aliases) {
    List<Attribute> attributes = getAttributes(name, aliases);

    if (attributes == null)
      return ExpressionList.EMPTY;

    for (Attribute attribute : attributes) {
      if (!(attribute instanceof ExpressionAttribute))
        throw new MarkupParseException(attribute.attributeName.finalName, MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE, formatNames(name, aliases), tagName.buildString());

      attribute.hasBeenUsed = true;
    }

    //noinspection unchecked
    return new ExpressionList((List<ExpressionAttribute>) (List<?>) attributes);
  }

  @Override
  public @NotNull ExpressionList getOptionalBoundFlagExpressionList() {
    return new ExpressionList(selectUnusedBoundFlagAttributesInOrder());
  }

  @Override
  public @NotNull MarkupList getMandatoryMarkupList(String name, String... aliases) {
    MarkupList result = getOptionalMarkupList(name, aliases);

    if (result.isEmpty())
      throw new MarkupParseException(tagName, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagName.buildString(), formatNames(name, aliases));

    return result;
  }

  @Override
  public @NotNull MarkupList getOptionalMarkupList(String name, String... aliases) {
    List<Attribute> attributes = getAttributes(name, aliases);

    if (attributes == null)
      return MarkupList.EMPTY;

    attributes.forEach(attribute -> attribute.hasBeenUsed = true);

    return new MarkupList(attributes);
  }

  @Override
  public @NotNull MarkupList getOptionalBoundFlagMarkupList() {
    return new MarkupList(selectUnusedBoundFlagAttributesInOrder());
  }

  private List<ExpressionAttribute> selectUnusedBoundFlagAttributesInOrder() {
    List<ExpressionAttribute> unusedAttributes = null;

    for (List<Attribute> bucket : attributeMap.values()) {
      for (Attribute attribute : bucket) {
        if (attribute.hasBeenUsed)
          continue;

        if (!attribute.attributeName.flags.contains(AttributeFlag.BINDING_MODE))
          continue;

        if (!attribute.attributeName.flags.contains(AttributeFlag.FLAG_STYLE))
          continue;

        // By definition, an expression is bound to an attribute, so we cannot encounter any other type at this point.
        if (!(attribute instanceof ExpressionAttribute))
          throw new IllegalStateException();

        if (unusedAttributes == null)
          unusedAttributes = new ArrayList<>();

        attribute.hasBeenUsed = true;

        unusedAttributes.add((ExpressionAttribute) attribute);
      }
    }

    if (unusedAttributes == null)
      return Collections.emptyList();

    unusedAttributes.sort(Comparator.comparingInt(attribute -> attribute.attributeName.fullName.startInclusive));

    return unusedAttributes;
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

    if (attribute.attributeName.flags.contains(AttributeFlag.BINDING_MODE) && attribute.attributeName.flags.contains(AttributeFlag.FLAG_STYLE))
      return name.replace('_', '-');

    return name;
  }
}
