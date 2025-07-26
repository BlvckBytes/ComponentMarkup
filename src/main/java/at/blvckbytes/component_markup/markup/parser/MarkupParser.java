package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.InterpolationNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionFlag;
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
import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

  private final @Nullable TokenOutput tokenOutput;
  private final TagRegistry tagRegistry;
  private final Stack<TagAndBuffers> tagStack;

  private StringPosition lastPosition;
  private final StringPosition initialPosition;
  private final boolean isSubParser;
  private @Nullable MarkupParser subtreeParser;
  private MarkupNode result;

  private MarkupParser(
    @Nullable TokenOutput tokenOutput,
    TagRegistry tagRegistry,
    StringPosition initialPosition,
    boolean isSubParser
  ) {
    this.tokenOutput = tokenOutput;
    this.tagRegistry = tagRegistry;
    this.tagStack = new Stack<>();
    this.lastPosition = initialPosition;
    this.initialPosition = initialPosition;
    this.isSubParser = isSubParser;
    this.result = new TextNode("", lastPosition);

    this.tagStack.push(new TagAndBuffers(null, null, lastPosition, null));
  }

  // ================================================================================
  // XML Event-Consumer
  // ================================================================================

  @Override
  public void onPosition(StringPosition position) {
    if (subtreeParser != null) {
      subtreeParser.lastPosition = position;
      return;
    }

    this.lastPosition = position;
  }

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
        throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG, tagName.buildString());

      tag = DummyTag.INSTANCE;
    }

    TagAndBuffers parent = tagStack.isEmpty() ? null : tagStack.peek();

    tagStack.push(new TagAndBuffers(tag, tagName, lastPosition, parent));
  }

  @Override
  public void onStringAttribute(StringView name, StringView value) {
    if (subtreeParser != null) {
      subtreeParser.onStringAttribute(name, value);
      return;
    }

    name.setLowercase();

    if (name.nthChar(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, value, null, true);
      return;
    }

    if (name.nthChar(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, parseExpression(value), null, false);
      return;
    }

    handleUserAttribute(name, value, value);
  }

  @Override
  public void onLongAttribute(StringView name, StringView raw, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, raw, value);
      return;
    }

    name.setLowercase();

    if (name.nthChar(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, null, true);
      return;
    }

    ExpressionNode immediateExpression = ImmediateExpression.ofLong(raw, value);

    if (name.nthChar(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value, false);
      return;
    }

    handleUserAttribute(name, immediateExpression, raw);
  }

  @Override
  public void onDoubleAttribute(StringView name, StringView raw, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, raw, value);
      return;
    }

    name.setLowercase();

    if (name.nthChar(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, value, true);
      return;
    }

    ExpressionNode immediateExpression = ImmediateExpression.ofDouble(raw, value);

    if (name.nthChar(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value, false);
      return;
    }

    handleUserAttribute(name, immediateExpression, raw);
  }

  @Override
  public void onBooleanAttribute(StringView name, StringView raw, boolean value) {
    if (subtreeParser != null) {
      subtreeParser.onBooleanAttribute(name, raw, value);
      return;
    }

    name.setLowercase();

    if (name.nthChar(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, value, true);
      return;
    }

    ExpressionNode immediateExpression = ImmediateExpression.ofBoolean(raw, value);

    if (name.nthChar(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value, false);
      return;
    }

    handleUserAttribute(name, immediateExpression, raw);
  }

  @Override
  public void onTagAttributeBegin(StringView name) {
    if (subtreeParser != null) {
      subtreeParser.onTagAttributeBegin(name);
      return;
    }

    subtreeParser = new MarkupParser(tokenOutput, tagRegistry, lastPosition, true);
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

    name.setLowercase();

    MarkupNode subtree = subtreeParser.result;
    StringPosition tagAttributeBeginPosition = subtreeParser.initialPosition;
    subtreeParser = null;

    if (name.nthChar(0) == '*') {
      handleIntrinsicAttribute(name, tagAttributeBeginPosition, subtree, null, false);
      return;
    }

    if (name.nthChar(0) == '+')
      throw new MarkupParseException(tagAttributeBeginPosition, MarkupParseError.LITERAL_INTRINSIC_MARKUP_ATTRIBUTE);

    handleUserAttribute(name, subtree, null);
  }

  @Override
  public void onFlagAttribute(StringView name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    name.setLowercase();

    if (name.nthChar(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, null, null, false);
      return;
    }

    if (name.nthChar(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, null, null, true);
      return;
    }

    handleUserAttribute(name, ImmediateExpression.ofBoolean(name, true), null);
  }

  @Override
  public void onTagOpenEnd(StringView tagName, boolean wasSelfClosing) {
    if (subtreeParser != null) {
      subtreeParser.onTagOpenEnd(tagName, wasSelfClosing);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();

    // Since a real tag has been pushed during the begin-call
    assert currentLayer.tagName != null;
    assert currentLayer.tag != null;

    TagClosing tagClosing = currentLayer.tag.tagClosing;

    if (!wasSelfClosing) {
      if (tagClosing == TagClosing.SELF_CLOSE)
        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SELF_CLOSING_TAG, currentLayer.tagName.buildString());

      return;
    }

    if (tagClosing == TagClosing.OPEN_CLOSE)
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_OPEN_CLOSE_TAG, currentLayer.tagName.buildString());

    tagStack.pop();
    tagStack.peek().addChild(currentLayer);
  }

  @Override
  public void onText(StringView text, EnumSet<SubstringFlag> flags) {
    if (subtreeParser != null) {
      subtreeParser.onText(text, flags);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.addChild(new TextNode(text.buildString(flags), lastPosition));
  }

  @Override
  public void onInterpolation(StringView expression) {
    if (subtreeParser != null) {
      subtreeParser.onInterpolation(expression);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.addChild(new InterpolationNode(parseExpression(expression), lastPosition));
  }

  @Override
  public void onTagClose(@Nullable StringView tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagClose(tagName);
      return;
    }

    boolean noOpUnmatched = tokenOutput != null && tokenOutput.outputFlags.contains(OutputFlag.UNMATCHED_CLOSING_TAGS_ARE_NO_OPS);

    if (tagName == null) {
      if (tagStack.size() <= 1) {
        if (noOpUnmatched)
          return;

        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK);
      }

      TagAndBuffers openedTag = tagStack.pop();
      tagStack.peek().addChild(openedTag);
      return;
    }

    if (tagName.contentEquals("*", true)) {
      if (tagStack.size() <= 1) {
        if (noOpUnmatched)
          return;

        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK, tagName.buildString());
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
        throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG, tagName.buildString());
    }

    if (noOpUnmatched) {
      if (tagStack.size() == 1)
        return;

      boolean didAnyMatch = false;

      for (int index = 1; index < tagStack.size(); ++index) {
        TagAndBuffers current = tagStack.get(index);

        if (current.tagName != null && tagName.contentEquals(current.tagName, true)) {
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
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG, tagName.buildString());

      tagStack.peek().addChild(openedTag);
    } while(openedTag.tagName == null || !tagName.contentEquals(openedTag.tagName, true));
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
        this.result = currentLayer.createNode();
        break;
      }

      tagStack.peek().addChild(currentLayer);
    }
  }

  // ================================================================================
  // Public API
  // ================================================================================

  public static MarkupNode parse(String input, TagRegistry tagRegistry) {
    return parse(input, tagRegistry, null);
  }

  public static MarkupNode parse(String input, TagRegistry tagRegistry, @Nullable TokenOutput tokenOutput) {
    StringView inputView = StringView.of(input);

    // The initial position, which will also be applied to the outermost implicit
    // container, is the zero-sentinel (unreachable by user-input)
    MarkupParser parser = new MarkupParser(tokenOutput, tagRegistry, inputView.viewStart, false);

    if (tokenOutput != null)
      tokenOutput.onInitialization(inputView);

    try {
      XmlEventParser.parse(inputView, parser, tokenOutput);
    } catch (XmlParseException xmlException) {
      throw new MarkupParseException(parser.lastPosition, xmlException);
    }

    return parser.result;
  }

  // ================================================================================
  // Utilities
  // ================================================================================

  /**
   * @param value null => flag; String => literal (+); MarkupNode | ExpressionNode => evaluated (*)
   */
  private void handleIntrinsicAttribute(
    StringView name,
    StringPosition attributePosition,
    @Nullable Object value,
    @Nullable Object immediateValue,
    boolean isLiteral
  ) {
    TagAndBuffers currentLayer = tagStack.peek();

    if (tokenOutput != null) {
      if (isLiteral)
        tokenOutput.emitCharToken(attributePosition, TokenType.MARKUP__OPERATOR__INTRINSIC_LITERAL);
      else
        tokenOutput.emitCharToken(attributePosition, TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION);
    }

    StringView fullName = name;
    name = name.buildSubViewUntilEnd(1);

    if (handleStaticallyNamedIntrinsicAttribute(fullName, attributePosition, value, immediateValue)) {
      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, name.buildSubViewRelative(1));

      return;
    }

    if (name.contentEquals("for", true) || name.startsWith("for-", true)) {
      if (!(value instanceof ExpressionNode) || immediateValue != null)
        throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName.buildString());

      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, name.buildSubViewRelative(0, 2));

      StringView iterationVariable = null;

      if (name.length() > 4) {
        iterationVariable = name.buildSubViewUntilEnd(4);

        if (isInvalidIdentifier(iterationVariable, true))
          throw new MarkupParseException(attributePosition, MarkupParseError.MALFORMED_IDENTIFIER, iterationVariable.buildString());

        if (tokenOutput != null) {
          tokenOutput.emitToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, name.buildSubViewRelative(3, 3));
          tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__BINDING, iterationVariable);
        }

        if (currentLayer.hasLetBinding(iterationVariable))
          throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, iterationVariable.buildString());
      }

      if (currentLayer.forIterable != null)
        throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_LOOPS);

      currentLayer.forIterable = (ExpressionNode) value;
      currentLayer.forIterationVariable = iterationVariable;
      return;
    }

    if (name.startsWith("let-", true)) {
      StringView bindingName = name.buildSubViewUntilEnd(4);

      if (tokenOutput != null) {
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, name.buildSubViewRelative(0, 2));
        tokenOutput.emitToken(TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, name.buildSubViewRelative(3, 3));
      }

      boolean isCaptureMode = false;

      int nameLength;

      while ((nameLength = bindingName.length()) > 0) {
        boolean hasOpening = bindingName.nthChar(0) == '(';
        boolean hasClosing = nameLength != 1 && bindingName.lastChar() == ')';

        if (!hasOpening && !hasClosing)
          break;

        if (!hasOpening || !hasClosing)
          throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES);

        if (isCaptureMode)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_CAPTURE_PARENTHESES);

        if (tokenOutput != null) {
          tokenOutput.emitCharToken(bindingName.viewStart, TokenType.MARKUP__OPERATOR__CAPTURE);
          tokenOutput.emitCharToken(bindingName.viewEnd, TokenType.MARKUP__OPERATOR__CAPTURE);
        }

        isCaptureMode = true;
        bindingName = bindingName.buildSubViewRelative(1, -1);
      }

      if (bindingName.isEmpty())
        throw new MarkupParseException(lastPosition, MarkupParseError.EMPTY_BINDING_NAME);

      if (isInvalidIdentifier(bindingName, true))
        throw new MarkupParseException(attributePosition, MarkupParseError.MALFORMED_IDENTIFIER, bindingName.buildString());

      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__BINDING, bindingName);

      if (currentLayer.forIterationVariable != null && bindingName.contentEquals(currentLayer.forIterationVariable, true))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, bindingName.buildString());

      if (value instanceof StringView) {
        StringView stringView = (StringView) value;
        value = ImmediateExpression.ofString(stringView, stringView.buildString());
      }

      LetBinding binding;

      if (value instanceof MarkupNode)
        binding = new MarkupLetBinding((MarkupNode) value, bindingName, isCaptureMode);
      else {
        if (isCaptureMode && immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_MARKUP_OR_EXPRESSION_CAPTURE, bindingName.buildString());

        if (value instanceof ExpressionNode)
          binding = new ExpressionLetBinding((ExpressionNode) value, isCaptureMode, bindingName);
        else
          throw new MarkupParseException(attributePosition, MarkupParseError.VALUELESS_BINDING, bindingName.buildString());
      }

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, bindingName.buildString());

      return;
    }

    throw new MarkupParseException(attributePosition, MarkupParseError.UNKNOWN_INTRINSIC_ATTRIBUTE, fullName.buildString());
  }

  private boolean handleStaticallyNamedIntrinsicAttribute(
    StringView name,
    StringPosition attributePosition,
    @Nullable Object value,
    @Nullable Object immediateValue
  ) {
    TagAndBuffers currentLayer = tagStack.peek();

    StringView fullName = name;
    name = name.buildSubViewUntilEnd(1);

    // Since attributes only occur on a real tag
    assert currentLayer.tagName != null;

    switch (name.buildString()) {
      case "if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = (ExpressionNode) value;
        currentLayer.ifConditionType = ConditionType.IF;
        return true;
      }

      case "else-if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = (ExpressionNode) value;
        currentLayer.ifConditionType = ConditionType.ELSE_IF;
        return true;
      }

      case "use": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.useCondition != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_USE_CONDITIONS);

        currentLayer.useCondition = (ExpressionNode) value;
        return true;
      }

      case "other": {
        if (value != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name.buildString());

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.isWhenOther || currentLayer.whenIsValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.isWhenOther = true;
        return true;
      }

      case "else": {
        if (value != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifConditionType = ConditionType.ELSE;
        return true;
      }

      case "when": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.whenInput != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT);

        currentLayer.whenInput = (ExpressionNode) value;
        return true;
      }

      case "is": {
        String isValue;

        if (value instanceof StringView)
          isValue = ((StringView) value).buildString();
        else if (immediateValue != null)
          isValue = String.valueOf(immediateValue);
        else
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_LITERAL_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.IS_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.whenIsValue != null || currentLayer.isWhenOther)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.whenIsValue = isValue;
        return true;
      }

      case "for-reversed":
        if (!(value instanceof ExpressionNode)) {
          if (value != null)
            throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName.buildString());

          // Let's allow a flag-style declaration, for convenience
          value = ImmediateExpression.ofBoolean(name, true);
        }

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.forReversed != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forReversed = (ExpressionNode) value;
        return true;

      case "for-empty":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, fullName.buildString());

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.forEmpty != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forEmpty = (MarkupNode) value;
        return true;

      case "for-separator":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, fullName.buildString());

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, fullName.buildString());

        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forSeparator = (MarkupNode) value;
        return true;

      case "for-":
        throw new MarkupParseException(attributePosition, MarkupParseError.UNNAMED_FOR_LOOP);

      case "let":
      case "let-":
        throw new MarkupParseException(attributePosition, MarkupParseError.UNNAMED_LET_BINDING);
    }

    return false;
  }

  private void handleUserAttribute(StringView name, @NotNull Object value, @Nullable StringView rawValue) {
    int nameLength;
    boolean isExpressionMode = false;

    while ((nameLength = name.length()) > 0) {
      boolean hasOpening = name.nthChar(0) == '[';
      boolean hasClosing = nameLength != 1 && name.lastChar() == ']';

      if (!hasOpening && !hasClosing)
        break;

      if (!hasOpening || !hasClosing)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

      if (isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_ATTRIBUTE_BRACKETS);

      if (tokenOutput != null) {
        tokenOutput.emitCharToken(name.viewStart, TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE);
        tokenOutput.emitCharToken(name.viewEnd, TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE);
      }

      isExpressionMode = true;
      name = name.buildSubViewRelative(1, -1);
    }

    if (isExpressionMode) {
      if (!name.isEmpty() && (name.nthChar(0) == '*' || name.nthChar(0) == '+'))
        throw new MarkupParseException(lastPosition, MarkupParseError.BRACKETED_INTRINSIC_ATTRIBUTE);
    }

    boolean isSpreadMode = false;

    while (!name.isEmpty() && name.nthChar(0) == '.') {
      if (!(name.nthChar(1) == '.' && name.nthChar(2) == '.'))
        throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_SPREAD_OPERATOR);

      if (isSpreadMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_ATTRIBUTE_SPREADS);

      StringView spreadOperator = name.buildSubViewRelative(0, 2);

      name = name.buildSubViewUntilEnd(3);
      isSpreadMode = true;

      if (!isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION, name.buildString());

      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__OPERATOR__SPREAD, spreadOperator);
    }

    if (name.isEmpty())
      throw new MarkupParseException(lastPosition, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

    if (isInvalidIdentifier(name, false))
      throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_ATTRIBUTE_NAME, name.buildString());

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, name);

    TagAndBuffers currentLayer = tagStack.peek();

    ExpressionNode expression;

    if (isExpressionMode) {
      if (!(value instanceof StringView))
        throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

      expression = parseExpression((StringView) value);
    }

    else if (value instanceof MarkupNode) {
      currentLayer.attributeMap.add(new MarkupAttribute(name, (MarkupNode) value));
      return;
    }

    else {
      if (value instanceof ExpressionNode)
        expression = (ExpressionNode) value;
      else {
        if (rawValue == null)
          throw new IllegalStateException("Require a raw-value to instantiate a proper immediate-expression on");

        expression = ImmediateExpression.ofString(rawValue, rawValue.buildString());
      }
    }

    ExpressionAttribute attribute = new ExpressionAttribute(name, expression);

    if (!isExpressionMode)
      attribute.flags.add(ExpressionFlag.IMMEDIATE_VALUE);

    if (isSpreadMode)
      attribute.flags.add(ExpressionFlag.SPREAD_MODE);

    currentLayer.attributeMap.add(attribute);
  }

  private boolean isInvalidIdentifier(StringView identifier, boolean expression) {
    int length = identifier.length();

    if (length == 0)
      return true;

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
        throw new MarkupParseException(lastPosition, MarkupParseError.EMPTY_EXPRESSION);

      return expression;
    } catch (ExpressionTokenizeException expressionTokenizeException) {
      throw new MarkupParseException(value.viewStart, expressionTokenizeException);
    } catch (ExpressionParseException expressionParseException) {
      throw new MarkupParseException(value.viewStart, expressionParseException);
    }
  }
}
