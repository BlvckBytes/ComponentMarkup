package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.InterpolationNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionFlag;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.ContainerTag;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.expression.tokenizer.token.BooleanToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.DoubleToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.LongToken;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.DummyTag;
import at.blvckbytes.component_markup.markup.parser.token.OutputFlag;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.markup.xml.XmlEventParser;
import at.blvckbytes.component_markup.markup.xml.XmlParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

  private final @Nullable TokenOutput tokenOutput;
  private final TagRegistry tagRegistry;
  private final Stack<TagAndBuffers> tagStack;

  private CursorPosition lastPosition;
  private final CursorPosition initialPosition;
  private final boolean isSubParser;
  private @Nullable MarkupParser subtreeParser;
  private MarkupNode result;

  private MarkupParser(
    @Nullable TokenOutput tokenOutput,
    TagRegistry tagRegistry,
    CursorPosition initialPosition,
    boolean isSubParser
  ) {
    this.tokenOutput = tokenOutput;
    this.tagRegistry = tagRegistry;
    this.tagStack = new Stack<>();
    this.lastPosition = initialPosition;
    this.initialPosition = initialPosition;
    this.isSubParser = isSubParser;
    this.result = new TextNode("", lastPosition);

    this.tagStack.push(new TagAndBuffers(ContainerTag.INSTANCE, ContainerTag.TAG_NAME, lastPosition, null));
  }

  // ================================================================================
  // XML Event-Consumer
  // ================================================================================

  @Override
  public void onCursorPosition(CursorPosition position) {
    if (subtreeParser != null) {
      subtreeParser.lastPosition = position;
      return;
    }

    this.lastPosition = position;
  }

  @Override
  public void onTagOpenBegin(String tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagOpenBegin(tagName);
      return;
    }

    tagName = lower(tagName);
    TagDefinition tag = tagRegistry.locateTag(tagName);

    if (tag == null) {
      if (tokenOutput == null || !tokenOutput.outputFlags.contains(OutputFlag.ENABLE_DUMMY_TAG))
        throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG, tagName);

      tag = DummyTag.INSTANCE;
    }

    TagAndBuffers parent = tagStack.isEmpty() ? null : tagStack.peek();

    tagStack.push(new TagAndBuffers(tag, tagName, lastPosition, parent));
  }

  @Override
  public void onStringAttribute(String name, CursorPosition valueBeginPosition, String value) {
    if (subtreeParser != null) {
      subtreeParser.onStringAttribute(name, valueBeginPosition, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, value, null, true);
      return;
    }

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, parseExpression(value, valueBeginPosition), null, false);
      return;
    }

    handleUserAttribute(name, value, lastPosition, valueBeginPosition);
  }

  @Override
  public void onLongAttribute(String name, String raw, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, raw, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, null, true);
      return;
    }

    ExpressionNode immediateExpression = new TerminalNode(new LongToken(0, raw, value));

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value, false);
      return;
    }

    handleUserAttribute(name, immediateExpression, lastPosition, null);
  }

  @Override
  public void onDoubleAttribute(String name, String raw, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, raw, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, value, true);
      return;
    }

    ExpressionNode immediateExpression = new TerminalNode(new DoubleToken(0, raw, value));

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value, false);
      return;
    }

    handleUserAttribute(name, immediateExpression, lastPosition, null);
  }

  @Override
  public void onBooleanAttribute(String name, String raw, boolean value) {
    if (subtreeParser != null) {
      subtreeParser.onBooleanAttribute(name, raw, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, value, true);
      return;
    }

    ExpressionNode immediateExpression = new TerminalNode(new BooleanToken(0, raw, value));

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value, false);
      return;
    }

    handleUserAttribute(name, immediateExpression, lastPosition, null);
  }

  @Override
  public void onTagAttributeBegin(String name) {
    if (subtreeParser != null) {
      subtreeParser.onTagAttributeBegin(name);
      return;
    }

    subtreeParser = new MarkupParser(tokenOutput, tagRegistry, lastPosition, true);
  }

  @Override
  public void onTagAttributeEnd(String name) {
    if (subtreeParser != null) {
      if (subtreeParser.subtreeParser != null) {
        subtreeParser.onTagAttributeEnd(name);
        return;
      }

      subtreeParser.onInputEnd();
    }

    else
      throw new IllegalStateException("Expected there to be a subtree-parser");

    name = lower(name);

    MarkupNode subtree = subtreeParser.result;
    CursorPosition tagAttributeBeginPosition = subtreeParser.initialPosition;
    subtreeParser = null;

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, tagAttributeBeginPosition, subtree, null, false);
      return;
    }

    if (name.charAt(0) == '+')
      throw new MarkupParseException(tagAttributeBeginPosition, MarkupParseError.LITERAL_INTRINSIC_MARKUP_ATTRIBUTE);

    handleUserAttribute(name, subtree, tagAttributeBeginPosition, null);
  }

  @Override
  public void onFlagAttribute(String name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, null, null, false);
      return;
    }

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, null, null, true);
      return;
    }

    handleUserAttribute(name, ImmediateExpression.of(true), lastPosition, null);
  }

  @Override
  public void onTagOpenEnd(String tagName, boolean wasSelfClosing) {
    if (subtreeParser != null) {
      subtreeParser.onTagOpenEnd(tagName, wasSelfClosing);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    TagClosing tagClosing = currentLayer.tag.tagClosing;

    if (!wasSelfClosing) {
      if (tagClosing == TagClosing.SELF_CLOSE)
        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SELF_CLOSING_TAG, currentLayer.tagNameLower);

      return;
    }

    if (tagClosing == TagClosing.OPEN_CLOSE)
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_OPEN_CLOSE_TAG, currentLayer.tagNameLower);

    tagStack.pop();
    tagStack.peek().addChild(currentLayer);
  }

  @Override
  public void onText(String text) {
    if (subtreeParser != null) {
      subtreeParser.onText(text);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.addChild(new TextNode(text, lastPosition));
  }

  @Override
  public void onInterpolation(String expression, CursorPosition valueBeginPosition) {
    if (subtreeParser != null) {
      subtreeParser.onInterpolation(expression, valueBeginPosition);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.addChild(new InterpolationNode(parseExpression(expression, valueBeginPosition), lastPosition));
  }

  @Override
  public void onTagClose(@Nullable String tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagClose(tagName);
      return;
    }

    if (tokenOutput != null && tokenOutput.outputFlags.contains(OutputFlag.IGNORE_CLOSING_TAGS))
      return;

    if (tagName == null) {
      if (tagStack.size() <= 1)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK);

      TagAndBuffers openedTag = tagStack.pop();
      tagStack.peek().addChild(openedTag);
      return;
    }

    if (tagName.equals("*")) {
      if (tagStack.size() <= 1)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK, tagName);

      while (tagStack.size() > 1) {
        TagAndBuffers openedTag = tagStack.pop();
        tagStack.peek().addChild(openedTag);
      }

      return;
    }

    tagName = lower(tagName);

    TagDefinition closedTag = tagRegistry.locateTag(tagName);

    if (closedTag == null) {
      if (tokenOutput == null || !tokenOutput.outputFlags.contains(OutputFlag.ENABLE_DUMMY_TAG))
        throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG, tagName);
    }

    TagAndBuffers openedTag;

    do {
      openedTag = tagStack.pop();

      if (tagStack.isEmpty())
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG, tagName);

      tagStack.peek().addChild(openedTag);
    } while(!openedTag.tagNameLower.equals(tagName));
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
    // The initial position, which will also be applied to the outermost implicit
    // container, is the zero-sentinel (unreachable by user-input)
    MarkupParser parser = new MarkupParser(tokenOutput, tagRegistry, new CursorPosition(0, 0, 0, input), false);

    if (tokenOutput != null)
      tokenOutput.onInitialization(input);

    try {
      XmlEventParser.parse(input, parser, tokenOutput);
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
    String name,
    CursorPosition attributePosition,
    @Nullable Object value,
    @Nullable Object immediateValue,
    boolean isLiteral
  ) {
    TagAndBuffers currentLayer = tagStack.peek();
    int attributeBeginIndex = attributePosition.nextCharIndex - 1;

    if (tokenOutput != null) {
      if (isLiteral)
        tokenOutput.emitToken(attributeBeginIndex, TokenType.MARKUP__OPERATOR__INTRINSIC_LITERAL, "+");
      else
        tokenOutput.emitToken(attributeBeginIndex, TokenType.MARKUP__OPERATOR__INTRINSIC_EXPRESSION, "*");
    }

    String fullName = name;
    name = name.substring(1);

    if (handleStaticallyNamedIntrinsicAttribute(fullName, attributePosition, value, immediateValue)) {
      if (tokenOutput != null)
        tokenOutput.emitToken(attributeBeginIndex + 1, TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, name);

      return;
    }

    if (name.equals("for") || name.startsWith("for-")) {
      if (!(value instanceof ExpressionNode) || immediateValue != null)
        throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

      if (tokenOutput != null)
        tokenOutput.emitToken(attributeBeginIndex + 1, TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, "for");

      String iterationVariable = null;

      if (name.length() > 4) {
        iterationVariable = name.substring(4);

        if (isInvalidIdentifier(iterationVariable, true))
          throw new MarkupParseException(attributePosition, MarkupParseError.MALFORMED_IDENTIFIER, iterationVariable);

        if (tokenOutput != null) {
          tokenOutput.emitToken(attributeBeginIndex + 4, TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, "-");
          tokenOutput.emitToken(attributeBeginIndex + 5, TokenType.MARKUP__IDENTIFIER__BINDING, iterationVariable);
        }
      }

      if (currentLayer.forIterable != null)
        throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_LOOPS);

      if (currentLayer.hasLetBinding(iterationVariable))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, iterationVariable);

      currentLayer.forIterable = (ExpressionNode) value;
      currentLayer.forIterationVariable = iterationVariable;
      return;
    }

    if (name.startsWith("let-")) {
      String bindingName = name.substring(4);

      if (tokenOutput != null) {
        tokenOutput.emitToken(attributeBeginIndex + 1, TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, "let");
        tokenOutput.emitToken(attributeBeginIndex + 4, TokenType.MARKUP__PUNCTUATION__BINDING_SEPARATOR, "-");
      }

      int nameBeginIndex = attributeBeginIndex + 5;

      boolean isCaptureMode = false;

      int nameLength;

      while ((nameLength = bindingName.length()) > 0) {
        boolean hasOpening = bindingName.charAt(0) == '(';
        boolean hasClosing = nameLength != 1 && bindingName.charAt(nameLength - 1) == ')';

        if (!hasOpening && !hasClosing)
          break;

        if (!hasOpening || !hasClosing)
          throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES);

        if (isCaptureMode)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_CAPTURE_PARENTHESES);

        if (tokenOutput != null)
          tokenOutput.emitToken(nameBeginIndex, TokenType.MARKUP__OPERATOR__CAPTURE, "(");

        isCaptureMode = true;
        bindingName = bindingName.substring(1, nameLength - 1);

        ++nameBeginIndex;
      }

      if (bindingName.isEmpty())
        throw new MarkupParseException(lastPosition, MarkupParseError.EMPTY_BINDING_NAME);

      if (isInvalidIdentifier(bindingName, true))
        throw new MarkupParseException(attributePosition, MarkupParseError.MALFORMED_IDENTIFIER, bindingName);

      if (tokenOutput != null) {
        if (isCaptureMode)
          tokenOutput.emitToken(nameBeginIndex + bindingName.length(), TokenType.MARKUP__OPERATOR__CAPTURE, ")");

        tokenOutput.emitToken(nameBeginIndex, TokenType.MARKUP__IDENTIFIER__BINDING, bindingName);
      }

      if (bindingName.equals(currentLayer.forIterationVariable))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, bindingName);

      if (value instanceof String)
        value = ImmediateExpression.of((String) value);

      LetBinding binding;

      if (value instanceof MarkupNode)
        binding = new MarkupLetBinding((MarkupNode) value, bindingName, isCaptureMode, attributePosition);
      else {
        if (isCaptureMode && immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_MARKUP_OR_EXPRESSION_CAPTURE, bindingName);

        if (value instanceof ExpressionNode)
          binding = new ExpressionLetBinding((ExpressionNode) value, isCaptureMode, bindingName, attributePosition);
        else
          throw new MarkupParseException(attributePosition, MarkupParseError.VALUELESS_BINDING, bindingName);
      }

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, bindingName);

      return;
    }

    throw new MarkupParseException(attributePosition, MarkupParseError.UNKNOWN_INTRINSIC_ATTRIBUTE, fullName);
  }

  private boolean handleStaticallyNamedIntrinsicAttribute(
    String name,
    CursorPosition attributePosition,
    @Nullable Object value,
    @Nullable Object immediateValue
  ) {
    TagAndBuffers currentLayer = tagStack.peek();

    String fullName = name;
    name = name.substring(1);

    switch (name) {
      case "if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

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
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = (ExpressionNode) value;
        currentLayer.ifConditionType = ConditionType.ELSE_IF;
        return true;
      }

      case "use": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.useCondition != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_USE_CONDITIONS);

        currentLayer.useCondition = (ExpressionNode) value;
        return true;
      }

      case "other": {
        if (value != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name);

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.isWhenOther || currentLayer.whenIsValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.isWhenOther = true;
        return true;
      }

      case "else": {
        if (value != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifConditionType = ConditionType.ELSE;
        return true;
      }

      case "when": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.whenInput != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT);

        currentLayer.whenInput = (ExpressionNode) value;
        return true;
      }

      case "is": {
        String isValue;

        if (value instanceof String)
          isValue = (String) value;
        else if (immediateValue != null)
          isValue = String.valueOf(immediateValue);
        else
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_LITERAL_INTRINSIC_ATTRIBUTE, fullName);

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
            throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

          // Let's allow a flag-style declaration, for convenience
          value = ImmediateExpression.of(true);
        }

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.forReversed != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName, currentLayer.tagNameLower);

        currentLayer.forReversed = (ExpressionNode) value;
        return true;

      case "for-empty":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, fullName);

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.forEmpty != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName, currentLayer.tagNameLower);

        currentLayer.forEmpty = (MarkupNode) value;
        return true;

      case "for-separator":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, fullName);

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName, currentLayer.tagNameLower);

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

  private void handleUserAttribute(String name, @NotNull Object value, CursorPosition attributeBeginPosition, @Nullable CursorPosition valueBeginPosition) {
    int nameLength;
    boolean isExpressionMode = false;

    int beginIndex = attributeBeginPosition.nextCharIndex - 1;
    int beginOffset = 0;

    while ((nameLength = name.length()) > 0) {
      boolean hasOpening = name.charAt(0) == '[';
      boolean hasClosing = nameLength != 1 && name.charAt(nameLength - 1) == ']';

      if (!hasOpening && !hasClosing)
        break;

      if (!hasOpening || !hasClosing)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

      if (isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_ATTRIBUTE_BRACKETS);

      isExpressionMode = true;
      name = name.substring(1, nameLength - 1);

      if (tokenOutput != null)
        tokenOutput.emitToken(beginIndex + beginOffset, TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, "[");

      ++beginOffset;
    }

    if (isExpressionMode) {
      if (!name.isEmpty() && (name.charAt(0) == '*' || name.charAt(0) == '+'))
        throw new MarkupParseException(lastPosition, MarkupParseError.BRACKETED_INTRINSIC_ATTRIBUTE);
    }

    boolean isSpreadMode = false;

    while (!name.isEmpty() && name.charAt(0) == '.') {
      if (!(name.charAt(1) == '.' && name.charAt(2) == '.'))
        throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_SPREAD_OPERATOR);

      if (isSpreadMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_ATTRIBUTE_SPREADS);

      name = name.substring(3);
      isSpreadMode = true;

      if (!isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION, name);

      if (tokenOutput != null)
        tokenOutput.emitToken(beginIndex + beginOffset, TokenType.MARKUP__OPERATOR__SPREAD, "...");

      beginOffset += 3;
    }

    if (name.isEmpty())
      throw new MarkupParseException(lastPosition, MarkupParseError.EMPTY_ATTRIBUTE_NAME);

    if (isInvalidIdentifier(name, false))
      throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_ATTRIBUTE_NAME, name);

    if (tokenOutput != null) {
      tokenOutput.emitToken(beginIndex + beginOffset, TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, name);

      if (isExpressionMode)
        tokenOutput.emitToken(beginIndex + beginOffset + name.length(), TokenType.MARKUP__OPERATOR__DYNAMIC_ATTRIBUTE, "]");
    }

    TagAndBuffers currentLayer = tagStack.peek();

    ExpressionNode expression;

    if (isExpressionMode) {
      if (!(value instanceof String))
        throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

      if (valueBeginPosition == null)
        throw new IllegalStateException("Expected valueBeginPosition to have been provided");

      expression = parseExpression((String) value, valueBeginPosition);
    }

    else if (value instanceof MarkupNode) {
      currentLayer.attributeMap.add(new MarkupAttribute(attributeBeginPosition, name, (MarkupNode) value));
      return;
    }

    else {
      if (value instanceof ExpressionNode)
        expression = (ExpressionNode) value;
      else
        expression = ImmediateExpression.of(String.valueOf(value));
    }

    ExpressionAttribute attribute = new ExpressionAttribute(lastPosition, name, expression);

    if (!isExpressionMode)
      attribute.flags.add(ExpressionFlag.IMMEDIATE_VALUE);

    if (isSpreadMode)
      attribute.flags.add(ExpressionFlag.SPREAD_MODE);

    currentLayer.attributeMap.add(attribute);
  }

  private boolean isInvalidIdentifier(String identifier, boolean expression) {
    int length = identifier.length();

    if (length == 0)
      return true;

    char separator = expression ? '_' : '-';

    for (int charIndex = 0; charIndex < length; ++charIndex) {
      char currentChar = identifier.charAt(charIndex);

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

  private String lower(String input) {
    int inputLength = input.length();
    String result = input;

    for (int charIndex = 0; charIndex < inputLength; ++charIndex) {
      char currentChar = input.charAt(charIndex);
      char currentLowerChar = Character.toLowerCase(currentChar);

      if (currentChar == currentLowerChar)
        continue;

      char[] resultData = new char[inputLength];

      input.getChars(0, charIndex, resultData, 0);

      resultData[charIndex] = currentLowerChar;

      while (++charIndex < inputLength)
        resultData[charIndex] = Character.toLowerCase(input.charAt(charIndex));

      result = new String(resultData);
      break;
    }

    return result;
  }

  private ExpressionNode parseExpression(String input, CursorPosition valueBeginPosition) {
    try {
      ExpressionNode expression = ExpressionParser.parse(input, valueBeginPosition.nextCharIndex - 1, tokenOutput);

      if (expression == null)
        throw new MarkupParseException(lastPosition, MarkupParseError.EMPTY_EXPRESSION);

      return expression;
    } catch (ExpressionTokenizeException expressionTokenizeException) {
      throw new MarkupParseException(valueBeginPosition, expressionTokenizeException);
    } catch (ExpressionParseException expressionParseException) {
      throw new MarkupParseException(valueBeginPosition, expressionParseException);
    }
  }
}
