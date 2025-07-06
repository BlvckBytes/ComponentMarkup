package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionFlag;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import at.blvckbytes.component_markup.markup.parser.MarkupParseError;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InternalAttributeMap implements AttributeMap {

  private final String tagNameLower;
  private final CursorPosition tagPosition;

  private final Map<String, List<Attribute>> attributeMap;

  public InternalAttributeMap(String tagNameLower, CursorPosition tagPosition) {
    this.attributeMap = new HashMap<>();
    this.tagNameLower = tagNameLower;
    this.tagPosition = tagPosition;
  }

  public void add(Attribute attribute) {
    this.attributeMap
      .computeIfAbsent(attribute.name, k -> new ArrayList<>())
      .add(attribute);
  }

  public void validateNoUnusedAttributes() {
    for (List<Attribute> attributeBucket : attributeMap.values()) {
      for (Attribute attribute : attributeBucket) {
        if (!attribute.hasBeenUsed)
          throw new MarkupParseException(attribute.position, MarkupParseError.UNSUPPORTED_ATTRIBUTE, tagNameLower, attribute.name);
      }
    }
  }

  // ================================================================================
  // NON_MULTI
  // ================================================================================

  @Override
  public @NotNull ExpressionNode getMandatoryExpressionNode(String name) {
    ExpressionNode result = getOptionalExpressionNode(name);

    if (result == null)
      throw new MarkupParseException(tagPosition, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagNameLower, name);

    return result;
  }

  @Override
  public @Nullable ExpressionNode getOptionalExpressionNode(String name) {
    Attribute attribute = selectNonMultiAttributeOrNull(name);

    if (attribute == null)
      return null;

    if (!(attribute instanceof ExpressionAttribute))
      throw new MarkupParseException(attribute.position, MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE, name, tagNameLower);

    ExpressionAttribute expressionAttribute = (ExpressionAttribute) attribute;

    if (expressionAttribute.flags.contains(ExpressionFlag.SPREAD_MODE))
      throw new MarkupParseException(attribute.position, MarkupParseError.SPREAD_ON_NON_MULTI_ATTRIBUTE, name, tagNameLower);

    return expressionAttribute.value;
  }

  @Override
  public @NotNull MarkupNode getMandatoryMarkupNode(String name) {
    MarkupNode result = getOptionalMarkupNode(name);

    if (result == null)
      throw new MarkupParseException(tagPosition, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagNameLower, name);

    return result;
  }

  @Override
  public @Nullable MarkupNode getOptionalMarkupNode(String name) {
    Attribute attribute = selectNonMultiAttributeOrNull(name);

    if (attribute == null)
      return null;

    if (!(attribute instanceof MarkupAttribute))
      throw new MarkupParseException(attribute.position, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, name, tagNameLower);

    return ((MarkupAttribute) attribute).value;
  }

  private @Nullable Attribute selectNonMultiAttributeOrNull(String name) {
    List<Attribute> attributes = attributeMap.get(name);

    if (attributes == null)
      return null;

    int attributesCount = attributes.size();

    if (attributesCount == 0)
      return null;

    if (attributesCount > 1)
      throw new MarkupParseException(attributes.get(1).position, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name, tagNameLower);

    Attribute result = attributes.get(0);

    result.hasBeenUsed = true;

    return result;
  }

  // ================================================================================
  // MULTI
  // ================================================================================

  @Override
  public @NotNull ExpressionList getMandatoryExpressionList(String name) {
    ExpressionList result = unwrapExpressionAttributes(selectMultiAttributeOrNull(name, true));

    if (result.isEmpty())
      throw new MarkupParseException(tagPosition, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagNameLower, name);

    return result;
  }

  @Override
  public @NotNull ExpressionList getOptionalExpressionList(String name) {
    return unwrapExpressionAttributes(selectMultiAttributeOrNull(name, true));
  }

  @Override
  public @NotNull MarkupList getMandatoryMarkupList(String name) {
    MarkupList result = unwrapMarkupAttributes(selectMultiAttributeOrNull(name, false));

    if (result.isEmpty())
      throw new MarkupParseException(tagPosition, MarkupParseError.MISSING_MANDATORY_ATTRIBUTE, tagNameLower, name);

    return result;
  }

  @Override
  public @NotNull MarkupList getOptionalMarkupList(String name) {
    return unwrapMarkupAttributes(selectMultiAttributeOrNull(name, false));
  }

  private @Nullable List<Attribute> selectMultiAttributeOrNull(String name, boolean expression) {
    List<Attribute> attributes = attributeMap.get(name);

    if (attributes == null)
      return null;

    for (Attribute attribute : attributes) {
      if (expression) {
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(attribute.position, MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE, name, tagNameLower);

        attribute.hasBeenUsed = true;
        continue;
      }

      if (!(attribute instanceof MarkupAttribute)) {
        // Immediate values are obviously nonsensical and were probably a mistake of the user
        // An expression-value will instantiate an expression-driven node later on
        if ((attribute instanceof ExpressionAttribute) && ((ExpressionAttribute) attribute).flags.contains(ExpressionFlag.IMMEDIATE_VALUE))
          throw new MarkupParseException(attribute.position, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, name, tagNameLower);
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
}
