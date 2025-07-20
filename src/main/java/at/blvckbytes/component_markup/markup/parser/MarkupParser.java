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
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.markup.xml.XmlEventParser;
import at.blvckbytes.component_markup.markup.xml.XmlParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

  private final TagRegistry tagRegistry;
  private final Stack<TagAndBuffers> tagStack;

  private CursorPosition lastPosition;
  private @Nullable CursorPosition tagAttributeBeginPosition;
  private @Nullable MarkupParser subtreeParser;
  private MarkupNode result;

  private MarkupParser(TagRegistry tagRegistry, CursorPosition initialPosition) {
    this.tagRegistry = tagRegistry;
    this.tagStack = new Stack<>();
    this.lastPosition = initialPosition;
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

    if (tag == null)
      throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG, tagName);

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
      handleIntrinsicAttribute(name, lastPosition, value, null);
      return;
    }

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, parseExpression(value, valueBeginPosition), null);
      return;
    }

    handleUserAttribute(name, value, valueBeginPosition);
  }

  @Override
  public void onLongAttribute(String name, String raw, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, raw, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, null);
      return;
    }

    ExpressionNode immediateExpression = new TerminalNode(new LongToken(0, raw, value));

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value);
      return;
    }

    handleUserAttribute(name, immediateExpression, null);
  }

  @Override
  public void onDoubleAttribute(String name, String raw, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, raw, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, value);
      return;
    }

    ExpressionNode immediateExpression = new TerminalNode(new DoubleToken(0, raw, value));

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value);
      return;
    }

    handleUserAttribute(name, immediateExpression, null);
  }

  @Override
  public void onBooleanAttribute(String name, String raw, boolean value) {
    if (subtreeParser != null) {
      subtreeParser.onBooleanAttribute(name, raw, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, raw, value);
      return;
    }

    ExpressionNode immediateExpression = new TerminalNode(new BooleanToken(0, raw, value));

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, immediateExpression, value);
      return;
    }

    handleUserAttribute(name, immediateExpression, null);
  }

  @Override
  public void onTagAttributeBegin(String name) {
    if (subtreeParser != null) {
      subtreeParser.onTagAttributeBegin(name);
      return;
    }

    this.tagAttributeBeginPosition = lastPosition;

    name = lower(name);

    if (name.charAt(0) == '[')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    subtreeParser = new MarkupParser(tagRegistry, lastPosition);
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
    subtreeParser = null;

    assert tagAttributeBeginPosition != null;

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, tagAttributeBeginPosition, subtree, null);
      return;
    }

    if (name.charAt(0) == '+')
      throw new IllegalStateException("Markup-values on plain intrinsic attributes make no sense");

    handleUserAttribute(name, subtree, null);
  }

  @Override
  public void onFlagAttribute(String name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '*') {
      handleIntrinsicAttribute(name, lastPosition, null, null);
      return;
    }

    if (name.charAt(0) == '+') {
      handleIntrinsicAttribute(name, lastPosition, null, null);
      return;
    }

    handleUserAttribute(name, ImmediateExpression.of(true), null);
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

    if (closedTag == null)
      throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG, tagName);

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
    if (subtreeParser != null)
      subtreeParser.onInputEnd();

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
    // The initial position, which will also be applied to the outermost implicit
    // container, is the zero-sentinel (unreachable by user-input)
    MarkupParser parser = new MarkupParser(tagRegistry, new CursorPosition(0, 0, 0, input));

    try {
      XmlEventParser.parse(input, parser);
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
  private void handleIntrinsicAttribute(String name, CursorPosition attributePosition, @Nullable Object value, @Nullable Object immediateValue) {
    TagAndBuffers currentLayer = tagStack.peek();

    // The prefix is already known as by the value's type
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
        return;
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
        return;
      }

      case "use": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.useCondition != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_USE_CONDITIONS);

        currentLayer.useCondition = (ExpressionNode) value;
        return;
      }

      case "other": {
        if (value != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name);

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(attributePosition, MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.isWhenOther || currentLayer.whenIsValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.isWhenOther = true;
        return;
      }

      case "else": {
        if (value != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifConditionType = ConditionType.ELSE;
        return;
      }

      case "when": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.whenInput != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT);

        currentLayer.whenInput = (ExpressionNode) value;
        return;
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
        return;
      }

      case "for-reversed":
        if (!(value instanceof ExpressionNode))
          throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

        if (currentLayer.forIterable == null)
          throw new IllegalStateException("The *for-reversed attribute may only be used in combination with for-loops");

        if (currentLayer.forReversed != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName, currentLayer.tagNameLower);

        currentLayer.forReversed = (ExpressionNode) value;
        return;

      case "for-separator":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(attributePosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, fullName);

        if (currentLayer.forIterable == null)
          throw new IllegalStateException("The *for-reversed attribute may only be used in combination with for-loops");

        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(attributePosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, fullName, currentLayer.tagNameLower);

        currentLayer.forSeparator = (MarkupNode) value;
        return;

      case "for-":
        throw new MarkupParseException(attributePosition, MarkupParseError.UNNAMED_FOR_LOOP);

      case "let":
      case "let-":
        throw new MarkupParseException(attributePosition, MarkupParseError.UNNAMED_LET_BINDING);
    }

    if (name.equals("for") || name.startsWith("for-")) {
      if (!(value instanceof ExpressionNode) || immediateValue != null)
        throw new MarkupParseException(attributePosition, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, fullName);

      String iterationVariable = null;

      if (name.length() > 4) {
        iterationVariable = name.substring(4);

        if (!isValidExpressionIdentifier(iterationVariable))
          throw new MarkupParseException(attributePosition, MarkupParseError.MALFORMED_IDENTIFIER, iterationVariable);
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

      if (!isValidExpressionIdentifier(bindingName))
        throw new MarkupParseException(attributePosition, MarkupParseError.MALFORMED_IDENTIFIER, bindingName);

      if (bindingName.equals(currentLayer.forIterationVariable))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, bindingName);

      if (value instanceof String)
        value = ImmediateExpression.of((String) value);

      LetBinding binding;

      if (value instanceof ExpressionNode)
        binding = new ExpressionLetBinding((ExpressionNode) value, bindingName, attributePosition);
      else if (value instanceof MarkupNode)
        binding = new MarkupLetBinding((MarkupNode) value, bindingName, attributePosition);
      else
        throw new IllegalStateException("Either supply an expression or a markup-value on let- bindings");

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(attributePosition, MarkupParseError.BINDING_IN_USE, bindingName);

      return;
    }

    throw new MarkupParseException(attributePosition, MarkupParseError.UNKNOWN_INTRINSIC_ATTRIBUTE, fullName);
  }

  private void handleUserAttribute(String name, @NotNull Object value, @Nullable CursorPosition valueBeginPosition) {
    int nameLength;
    boolean isExpressionMode = false;

    while ((nameLength = name.length()) > 2) {
      boolean hasOpening = name.charAt(0) == '[';
      boolean hasClosing = name.charAt(nameLength - 1) == ']';

      if (!hasOpening && !hasClosing)
        break;

      if (!hasOpening || !hasClosing)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

      if (isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_ATTRIBUTE_BRACKETS);

      isExpressionMode = true;
      name = name.substring(1, nameLength - 1);

      if (name.charAt(0) == '*' || name.charAt(0) == '+')
        throw new MarkupParseException(lastPosition, MarkupParseError.BRACKETED_INTRINSIC_ATTRIBUTE);
    }

    boolean isSpreadMode = false;

    if (name.length() > 3 && name.charAt(0) == '.' && name.charAt(1) == '.' && name.charAt(2) == '.') {
      name = name.substring(3);
      isSpreadMode = true;

      if (!isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION, name);
    }

    TagAndBuffers currentLayer = tagStack.peek();

    if (value instanceof MarkupNode) {
      currentLayer.attributeMap.add(new MarkupAttribute(tagAttributeBeginPosition, name, (MarkupNode) value));
      return;
    }

    ExpressionNode expression;

    if (isExpressionMode) {
      if (!(value instanceof String))
        throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

      expression = parseExpression((String) value, valueBeginPosition);
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

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean isValidExpressionIdentifier(String identifier) {
    int length = identifier.length();

    if (length == 0)
      return false;

    for (int charIndex = 0; charIndex < length; ++charIndex) {
      char currentChar = identifier.charAt(charIndex);

      if (charIndex != 0) {
        if (currentChar == '_' || (currentChar >= '0' && currentChar <= '9'))
          continue;
      }

      if (currentChar >= 'a' && currentChar <= 'z')
        continue;

      return false;
    }

    return true;
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
      ExpressionNode expression = ExpressionParser.parse(input);

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
