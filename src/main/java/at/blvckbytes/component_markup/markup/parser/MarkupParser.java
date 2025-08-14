/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.InterpolationNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.DummyTag;
import at.blvckbytes.component_markup.markup.parser.token.OutputFlag;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.markup.xml.XmlEventParser;
import at.blvckbytes.component_markup.markup.xml.XmlParseException;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

  public static final Set<String> RESERVED_OPERATOR_NAMES;

  static {
    RESERVED_OPERATOR_NAMES = new HashSet<>();
    RESERVED_OPERATOR_NAMES.addAll(InfixOperator.RESERVED_NAMES);
    RESERVED_OPERATOR_NAMES.addAll(PrefixOperator.RESERVED_NAMES);
    RESERVED_OPERATOR_NAMES.add("true");
    RESERVED_OPERATOR_NAMES.add("false");
    RESERVED_OPERATOR_NAMES.add("null");
  }

  private final StringView rootView;
  private final @Nullable TokenOutput tokenOutput;
  private final TagRegistry tagRegistry;
  private final Stack<TagAndBuffers> tagStack;

  private final boolean isSubParser;
  private @Nullable MarkupParser subtreeParser;
  private MarkupNode result;

  private MarkupParser(
    StringView rootView,
    @Nullable TokenOutput tokenOutput,
    TagRegistry tagRegistry,
    int initialPosition,
    boolean isSubParser
  ) {
    this.rootView = rootView;
    this.tokenOutput = tokenOutput;
    this.tagRegistry = tagRegistry;
    this.tagStack = new Stack<>();
    this.isSubParser = isSubParser;
    this.result = new TextNode(StringView.EMPTY, "");

    this.tagStack.push(new TagAndBuffers(rootView.endExclusive == 0 ? rootView : rootView.buildSubViewAbsolute(initialPosition, initialPosition)));
  }

  // ================================================================================
  // XML Event-Consumer
  // ================================================================================

  @Override
  public void onTagOpenBegin(StringView tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagOpenBegin(tagName);
      return;
    }

    tagName.setLowercase();

    TagDefinition tag = tagRegistry.locateTag(tagName);

    if (tag == null) {
      if (tokenOutput == null || !tokenOutput.outputFlags.contains(OutputFlag.ENABLE_DUMMY_TAG))
        throw new MarkupParseException(tagName, MarkupParseError.UNKNOWN_TAG, tagName.buildString());

      tag = DummyTag.INSTANCE;
    }

    TagAndBuffers parent = tagStack.isEmpty() ? null : tagStack.peek();
    tagStack.push(new TagAndBuffers(tag, tagName, parent));
  }

  @Override
  public void onStringAttribute(StringView name, StringView value) {
    if (subtreeParser != null) {
      subtreeParser.onStringAttribute(name, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);

    Attribute attribute;

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION) || attributeName.has(AttributeFlag.BINDING_MODE))
      attribute = new ExpressionAttribute(attributeName, parseExpression(value));
    else
      attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofString(value, value.buildString()));

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    handleUserAttribute(attribute);
  }

  @Override
  public void onTemplateLiteralAttribute(StringView name, TerminalNode value) {
    if (subtreeParser != null) {
      subtreeParser.onTemplateLiteralAttribute(name, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL))
      throw new MarkupParseException(name, MarkupParseError.LITERAL_INTRINSIC_TEMPLATE_LITERAL_ATTRIBUTE);

    Attribute attribute = new ExpressionAttribute(attributeName, value);

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    if (attributeName.has(AttributeFlag.BINDING_MODE))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleUserAttribute(attribute);
  }

  @Override
  public void onLongAttribute(StringView name, StringView raw, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, raw, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);
    Attribute attribute;

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofString(raw, raw.buildString()));
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofLong(raw, value));

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    if (attributeName.has(AttributeFlag.BINDING_MODE))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleUserAttribute(attribute);
  }

  @Override
  public void onDoubleAttribute(StringView name, StringView raw, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, raw, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);
    Attribute attribute;

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofString(raw, raw.buildString()));
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofDouble(raw, value));

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    if (attributeName.has(AttributeFlag.BINDING_MODE))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleUserAttribute(attribute);
  }

  @Override
  public void onBooleanAttribute(StringView name, StringView raw, boolean value) {
    if (subtreeParser != null) {
      subtreeParser.onBooleanAttribute(name, raw, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);
    Attribute attribute;

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofString(raw, raw.buildString()));
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofBoolean(raw, value));

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    if (attributeName.has(AttributeFlag.BINDING_MODE))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleUserAttribute(attribute);
  }

  @Override
  public void onNullAttribute(StringView name, StringView raw) {
    if (subtreeParser != null) {
      subtreeParser.onNullAttribute(name, raw);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);
    Attribute attribute;

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofString(raw, raw.buildString()));
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofNull());

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    handleUserAttribute(attribute);
  }

  @Override
  public void onTagAttributeBegin(StringView name, int valueBeginPosition) {
    if (subtreeParser != null) {
      subtreeParser.onTagAttributeBegin(name, valueBeginPosition);
      return;
    }

    subtreeParser = new MarkupParser(rootView, tokenOutput, tagRegistry, valueBeginPosition, true);
  }

  @Override
  public void onTagAttributeEnd(StringView name) {
    if (subtreeParser != null) {
      if (subtreeParser.subtreeParser != null) {
        subtreeParser.onTagAttributeEnd(name);
        return;
      }

      subtreeParser.onInputEnd();
    }

    else
      throw new IllegalStateException("Expected there to be a subtree-parser");

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);

    MarkupNode subtree = subtreeParser.result;
    subtreeParser = null;

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL))
      throw new MarkupParseException(name, MarkupParseError.LITERAL_INTRINSIC_MARKUP_ATTRIBUTE);

    Attribute attribute = new MarkupAttribute(attributeName, subtree);

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, false);
      return;
    }

    if (attributeName.has(AttributeFlag.BINDING_MODE))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleUserAttribute(attribute);
  }

  @Override
  public void onFlagAttribute(StringView name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput);

    ExpressionAttribute attribute = new ExpressionAttribute(attributeName, ImmediateExpression.ofBoolean(attributeName.finalName, true));

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attribute, true);
      return;
    }

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      handleIntrinsicAttribute(attribute, true);
      return;
    }

    if (attributeName.has(AttributeFlag.BINDING_MODE))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleUserAttribute(attribute);
  }

  @Override
  public void onTagOpenEnd(StringView tagName, boolean wasSelfClosing) {
    if (subtreeParser != null) {
      subtreeParser.onTagOpenEnd(tagName, wasSelfClosing);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();

    assert currentLayer.tag != null;
    assert currentLayer.tagName != null;

    TagClosing tagClosing = currentLayer.tag.tagClosing;

    if (!wasSelfClosing) {
      if (tagClosing == TagClosing.SELF_CLOSE)
        throw new MarkupParseException(tagName, MarkupParseError.EXPECTED_SELF_CLOSING_TAG, currentLayer.tagName.buildString());

      return;
    }

    if (tagClosing == TagClosing.OPEN_CLOSE)
      throw new MarkupParseException(tagName, MarkupParseError.EXPECTED_OPEN_CLOSE_TAG, currentLayer.tagName.buildString());

    tagStack.pop();
    tagStack.peek().addChild(currentLayer);
  }

  @Override
  public void onText(StringView text) {
    if (subtreeParser != null) {
      subtreeParser.onText(text);
      return;
    }

    tagStack.peek().addChild(new TextNode(text, text.buildString()));
  }

  @Override
  public void onInterpolation(ExpressionNode expression, StringView raw) {
    if (subtreeParser != null) {
      subtreeParser.onInterpolation(expression, raw);
      return;
    }

    tagStack.peek().addChild(new InterpolationNode(expression));
  }

  @Override
  public void onTagClose(@Nullable StringView tagName, int pointyPosition) {
    if (subtreeParser != null) {
      subtreeParser.onTagClose(tagName, pointyPosition);
      return;
    }

    boolean noOpUnmatched = tokenOutput != null && tokenOutput.outputFlags.contains(OutputFlag.UNMATCHED_CLOSING_TAGS_ARE_NO_OPS);

    if (tagName == null) {
      if (tagStack.size() <= 1) {
        if (noOpUnmatched)
          return;

        throw new MarkupParseException(pointyPosition, MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK);
      }

      TagAndBuffers openedTag = tagStack.pop();
      tagStack.peek().addChild(openedTag);
      return;
    }

    tagName.setLowercase();

    if (tagName.contentEquals("*", true)) {
      if (tagStack.size() <= 1) {
        if (noOpUnmatched)
          return;

        throw new MarkupParseException(tagName, MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK, tagName.buildString());
      }

      while (tagStack.size() > 1) {
        TagAndBuffers openedTag = tagStack.pop();
        tagStack.peek().addChild(openedTag);
      }

      return;
    }

    TagDefinition closedTag = tagRegistry.locateTag(tagName);

    if (closedTag == null) {
      if (tokenOutput == null || !tokenOutput.outputFlags.contains(OutputFlag.ENABLE_DUMMY_TAG))
        throw new MarkupParseException(tagName, MarkupParseError.UNKNOWN_TAG, tagName.buildString());
    }

    if (noOpUnmatched) {
      boolean didAnyMatch = false;

      for (TagAndBuffers current : tagStack) {
        if (current.tagName == null)
          continue;

        if (tagName.contentEquals(current.tagName.buildString(), true)) {
          didAnyMatch = true;
          break;
        }
      }

      if (!didAnyMatch)
        return;
    }

    TagAndBuffers openedTag;

    do {
      openedTag = tagStack.pop();

      if (tagStack.isEmpty())
        throw new MarkupParseException(tagName, MarkupParseError.UNBALANCED_CLOSING_TAG, tagName.buildString());

      tagStack.peek().addChild(openedTag);
    } while (openedTag.tagName == null || !tagName.contentEquals(openedTag.tagName.buildString(), true));
  }

  @Override
  public void onInputEnd() {
    if (subtreeParser != null) {
      subtreeParser.onInputEnd();
      return;
    }

    if (tokenOutput != null && !isSubParser)
      tokenOutput.onInputEnd();

    while (true) {
      TagAndBuffers currentLayer = tagStack.pop();

      if (tagStack.isEmpty()) {
        try {
          this.result = currentLayer.createNode();
        } catch (MarkupParseException parseError) {
          if (
            tokenOutput != null
              && parseError.error == MarkupParseError.MISSING_MANDATORY_ATTRIBUTE
              && tokenOutput.outputFlags.contains(OutputFlag.ALLOW_MISSING_ATTRIBUTES)
          )
            result = new TextNode(StringView.EMPTY, "<error>");
          else
            throw parseError;
        }
        break;
      }

      tagStack.peek().addChild(currentLayer);
    }
  }

  // ================================================================================
  // Public API
  // ================================================================================

  public static MarkupNode parse(StringView rootView, TagRegistry tagRegistry) {
    return parse(rootView, tagRegistry, null);
  }

  public static MarkupNode parse(StringView rootView, TagRegistry tagRegistry, @Nullable TokenOutput tokenOutput) {
    // The initial position, which will also be applied to the outermost implicit
    // container, is the zero-sentinel (unreachable by user-input)
    MarkupParser parser = new MarkupParser(rootView, tokenOutput, tagRegistry, rootView.startInclusive, false);

    if (tokenOutput != null)
      tokenOutput.onInitialization(rootView);

    try {
      XmlEventParser.parse(rootView, parser, tokenOutput);
    } catch (XmlParseException xmlException) {
      throw new MarkupParseException(xmlException).setRootView(rootView);
    } catch (MarkupParseException markupParseException) {
      markupParseException.setRootView(rootView);
      throw markupParseException;
    }

    return parser.result;
  }

  // ================================================================================
  // Utilities
  // ================================================================================

  private void handleIntrinsicAttribute(Attribute attribute, boolean wasFlag) {
    TagAndBuffers currentLayer = tagStack.peek();
    AttributeName attributeName = attribute.attributeName;

    if (handleStaticallyNamedIntrinsicAttribute(attribute, wasFlag)) {
      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, attributeName.finalName);

      return;
    }

    if (attributeName.finalName.contentEquals("for", true) || attributeName.finalName.startsWith("for-", true)) {
      if (!(attribute instanceof ExpressionAttribute))
        throw new MarkupParseException(attributeName.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, attributeName.fullName.buildString());

      if (wasFlag)
        throw new MarkupParseException(attributeName.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG, attributeName.fullName.buildString());

      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, attributeName.finalName.buildSubViewRelative(0, 3));

      StringView iterationVariable = null;

      if (attributeName.finalName.length() > 4) {
        iterationVariable = attributeName.finalName.buildSubViewRelative(4);

        if (isInvalidIdentifier(iterationVariable, true))
          throw new MarkupParseException(iterationVariable, MarkupParseError.MALFORMED_IDENTIFIER, iterationVariable.buildString());

        if (tokenOutput != null) {
          tokenOutput.emitToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, attributeName.finalName.buildSubViewRelative(3, 4));
          tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__BINDING, iterationVariable);
        }

        if (currentLayer.hasLetBinding(iterationVariable.buildString()))
          throw new MarkupParseException(iterationVariable, MarkupParseError.BINDING_IN_USE, iterationVariable.buildString());
      }

      if (currentLayer.forIterable != null)
        throw new MarkupParseException(attributeName.fullName, MarkupParseError.MULTIPLE_LOOPS);

      currentLayer.forAttribute = attributeName.fullName;
      currentLayer.forIterable = ((ExpressionAttribute) attribute).value;
      currentLayer.forIterationVariable = iterationVariable;
      return;
    }

    if (attributeName.finalName.startsWith("let-", true)) {
      StringView bindingName = attributeName.finalName.buildSubViewRelative(4);

      if (tokenOutput != null) {
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, attributeName.finalName.buildSubViewRelative(0, 3));
        tokenOutput.emitToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, attributeName.finalName.buildSubViewRelative(3, 4));
      }

      boolean isCaptureMode = false;

      int nameLength;

      while ((nameLength = bindingName.length()) > 0) {
        boolean hasOpening = bindingName.nthChar(0) == '(';
        boolean hasClosing = nameLength != 1 && bindingName.nthChar(bindingName.length() - 1) == ')';

        if (!hasOpening && !hasClosing)
          break;

        if (!hasOpening || !hasClosing) {
          throw new MarkupParseException(
            hasOpening ? bindingName.startInclusive : bindingName.endExclusive - 1,
            MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES
          );
        }

        if (isCaptureMode)
          throw new MarkupParseException(bindingName, MarkupParseError.MULTIPLE_CAPTURE_PARENTHESES);

        if (tokenOutput != null) {
          tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__CAPTURE, bindingName.buildSubViewRelative(0, 1));
          tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__CAPTURE, bindingName.buildSubViewRelative(-1));
        }

        isCaptureMode = true;
        bindingName = bindingName.buildSubViewRelative(1, -1);
      }

      if (nameLength == 0)
        throw new MarkupParseException(attributeName.finalName, MarkupParseError.EMPTY_BINDING_NAME);

      if (isInvalidIdentifier(bindingName, true))
        throw new MarkupParseException(bindingName, MarkupParseError.MALFORMED_IDENTIFIER, bindingName.buildString());

      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__BINDING, bindingName);

      if (currentLayer.forIterationVariable != null && bindingName.contentEquals(currentLayer.forIterationVariable.buildString(), true))
        throw new MarkupParseException(bindingName, MarkupParseError.BINDING_IN_USE, bindingName.buildString());

      LetBinding binding;

      if (wasFlag)
        throw new MarkupParseException(attributeName.fullName, MarkupParseError.VALUELESS_BINDING, bindingName.buildString());

      if (attribute instanceof MarkupAttribute)
        binding = new MarkupLetBinding(((MarkupAttribute) attribute).value, bindingName, isCaptureMode);
      else if (attribute instanceof ExpressionAttribute)
        binding = new ExpressionLetBinding(((ExpressionAttribute) attribute).value, bindingName, isCaptureMode);
      else
        throw new IllegalStateException("Unexpected attribute-type: " + attribute.getClass());

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(bindingName, MarkupParseError.BINDING_IN_USE, bindingName.buildString());

      return;
    }

    throw new MarkupParseException(attributeName.fullName, MarkupParseError.UNKNOWN_INTRINSIC_ATTRIBUTE, attributeName.fullName.buildString());
  }

  private boolean handleStaticallyNamedIntrinsicAttribute(Attribute attribute, boolean wasFlag) {
    TagAndBuffers currentLayer = tagStack.peek();

    assert currentLayer.tagName != null;

    AttributeName name = attribute.attributeName;

    switch (name.finalName.buildString()) {
      case "if": {
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG, name.fullName.buildString());

        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = ((ExpressionAttribute) attribute).value;
        currentLayer.ifConditionType = ConditionType.IF;
        return true;
      }

      case "else-if": {
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG, name.fullName.buildString());

        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = ((ExpressionAttribute) attribute).value;
        currentLayer.ifConditionType = ConditionType.ELSE_IF;
        return true;
      }

      case "use": {
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG, name.fullName.buildString());

        if (currentLayer.useCondition != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_USE_CONDITIONS);

        currentLayer.useCondition = ((ExpressionAttribute) attribute).value;
        return true;
      }

      case "other": {
        if (!wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name.fullName.buildString());

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.isWhenOther || currentLayer.whenIsValue != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.isWhenOther = true;
        return true;
      }

      case "else": {
        if (!wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name.fullName.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifConditionType = ConditionType.ELSE;
        return true;
      }

      case "when": {
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG, name.fullName.buildString());

        if (currentLayer.whenInput != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT);

        currentLayer.whenInput = ((ExpressionAttribute) attribute).value;
        return true;
      }

      case "is": {
        if (!attribute.attributeName.has(AttributeFlag.INTRINSIC_LITERAL) || wasFlag)
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_LITERAL_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.IS_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.whenIsValue != null || currentLayer.isWhenOther)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.whenIsValue = ((TerminalNode) ((ExpressionAttribute) attribute).value).token.raw;
        return true;
      }

      case "for-reversed":
        if (!(attribute instanceof ExpressionAttribute))
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forReversed != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name.fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forReversed = ((ExpressionAttribute) attribute).value;
        return true;

      case "for-empty":
        if (currentLayer.forIterable == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forEmpty != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name.fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forEmpty = attribute.asMarkupNode();
        return true;

      case "for-separator":
        if (currentLayer.forIterable == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name.fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forSeparator = attribute.asMarkupNode();
        return true;

      case "for-":
        throw new MarkupParseException(name.fullName, MarkupParseError.UNNAMED_FOR_LOOP);

      case "let":
      case "let-":
        throw new MarkupParseException(name.fullName, MarkupParseError.UNNAMED_LET_BINDING);
    }

    return false;
  }

  private void handleUserAttribute(Attribute attribute) {
    AttributeName attributeName = attribute.attributeName;

    if (isInvalidIdentifier(attributeName.finalName, false))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.MALFORMED_ATTRIBUTE_NAME, attributeName.finalName.buildString());

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, attributeName.finalName);

    TagAndBuffers currentLayer = tagStack.peek();

    assert currentLayer.attributeMap != null;

    if (attribute instanceof MarkupAttribute) {
      currentLayer.attributeMap.add(new MarkupAttribute(attributeName, ((MarkupAttribute) attribute).value));
      return;
    }

    if (attribute instanceof ExpressionAttribute) {
      currentLayer.attributeMap.add(new ExpressionAttribute(attributeName, ((ExpressionAttribute) attribute).value));
      return;
    }

    throw new IllegalStateException("Unexpected attribute-type: " + attribute.getClass());
  }

  private boolean isInvalidIdentifier(StringView identifier, boolean expression) {
    int length = identifier.length();

    if (length == 0)
      return true;

    if (expression && RESERVED_OPERATOR_NAMES.contains(identifier.buildString()))
      throw new MarkupParseException(identifier, MarkupParseError.RESERVED_IDENTIFIER, identifier.buildString());

    char separator = expression ? '_' : '-';

    for (int charIndex = 0; charIndex < length; ++charIndex) {
      char currentChar = identifier.nthChar(charIndex);

      if (charIndex != 0) {
        if (currentChar == separator || (currentChar >= '0' && currentChar <= '9'))
          continue;
      }

      if (currentChar >= 'a' && currentChar <= 'z')
        continue;

      return true;
    }

    return false;
  }

  private ExpressionNode parseExpression(StringView value) {
    try {
      ExpressionNode expression = ExpressionParser.parse(value, tokenOutput);

      if (expression == null)
        throw new MarkupParseException(value, MarkupParseError.EMPTY_EXPRESSION);

      return expression;
    } catch (ExpressionTokenizeException expressionTokenizeException) {
      throw new MarkupParseException(value.startInclusive, expressionTokenizeException);
    } catch (ExpressionParseException expressionParseException) {
      throw new MarkupParseException(value.startInclusive, expressionParseException);
    }
  }
}
