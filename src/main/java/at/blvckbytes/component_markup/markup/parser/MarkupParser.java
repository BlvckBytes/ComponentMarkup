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
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

  private final TagRegistry tagRegistry;
  private final Stack<TagAndBuffers> tagStack;

  private CursorPosition lastPosition;
  private @Nullable CursorPosition tagAttributeBeginPosition;
  private @Nullable MarkupParser subtreeParser;
  private MarkupNode result;

  private MarkupParser(TagRegistry tagRegistry) {
    this(tagRegistry, CursorPosition.ZERO);
  }

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

    if (name.charAt(0) == '*') {
      handleStructuralAttribute(name, value, valueBeginPosition);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();

    int nameLength = name.length();

    if (name.equals("let"))
      throw new MarkupParseException(lastPosition, MarkupParseError.UNNAMED_LET_BINDING);

    if (name.startsWith("let-")) {
      if (nameLength == 4)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNNAMED_LET_BINDING);

      String bindingName = name.substring(4);

      if (!isValidExpressionIdentifier(bindingName))
        throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_IDENTIFIER, bindingName);

      if (bindingName.equals(currentLayer.forIterationVariable))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE, bindingName);

      ExpressionNode bindingExpression = parseExpression(value, valueBeginPosition);
      LetBinding binding = new ExpressionLetBinding(bindingExpression, bindingName, lastPosition);

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE, bindingName);

      return;
    }

    boolean isExpressionMode = false;
    boolean isSpreadMode = false;

    if (nameLength > 2 && name.charAt(0) == '[') {
      if (name.charAt(nameLength - 1) != ']')
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

      isExpressionMode = true;
      name = name.substring(1, nameLength - 1);
    }

    if (name.length() > 3 && name.charAt(0) == '.' && name.charAt(1) == '.' && name.charAt(2) == '.') {
      name = name.substring(3);
      isSpreadMode = true;

      if (!isExpressionMode)
        throw new MarkupParseException(lastPosition, MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION, name);
    }

    ExpressionNode expression = isExpressionMode ? parseExpression(value, valueBeginPosition) : ImmediateExpression.of(value);

    if (currentLayer.forIterable != null) {
      if (name.equals("for-reversed")) {
        if (currentLayer.forReversed != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name, currentLayer.tagNameLower);

        currentLayer.forReversed = expression;
        return;
      }

      if (name.equals("for-separator")) {
        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name, currentLayer.tagNameLower);

        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, name);
      }
    }

    ExpressionAttribute attribute = new ExpressionAttribute(lastPosition, name, expression);

    if (!isExpressionMode)
      attribute.flags.add(ExpressionFlag.IMMEDIATE_VALUE);

    if (isSpreadMode)
      attribute.flags.add(ExpressionFlag.SPREAD_MODE);

    currentLayer.attributeMap.add(attribute);
  }

  @Override
  public void onLongAttribute(String name, String raw, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, raw, value);
      return;
    }

    handleScalarNonStringAttribute(name, new TerminalNode(new LongToken(0, raw, value)));
  }

  @Override
  public void onDoubleAttribute(String name, String raw, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, raw, value);
      return;
    }

    handleScalarNonStringAttribute(name, new TerminalNode(new DoubleToken(0, raw, value)));
  }

  @Override
  public void onBooleanAttribute(String name, String raw, boolean value) {
    if (subtreeParser != null) {
      subtreeParser.onBooleanAttribute(name, raw, value);
      return;
    }

    handleScalarNonStringAttribute(name, new TerminalNode(new BooleanToken(0, raw, value)));
  }

  @Override
  public void onTagAttributeBegin(String name) {
    if (subtreeParser != null) {
      subtreeParser.onTagAttributeBegin(name);
      return;
    }

    this.tagAttributeBeginPosition = lastPosition;

    name = lower(name);

    if (name.charAt(0) == '*')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

    if (name.charAt(0) == '[')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    if (name.equals("let") || (name.startsWith("let-") && name.length() == 4))
      throw new MarkupParseException(lastPosition, MarkupParseError.UNNAMED_LET_BINDING);

    TagAndBuffers currentLayer = tagStack.peek();

    if (name.equals("for-separator") && currentLayer.forIterable != null) {
      if (currentLayer.forSeparator != null)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name, currentLayer.tagNameLower);
    }

    else if (name.equals("for-reversed"))
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE, name, currentLayer.tagNameLower);

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

    TagAndBuffers currentLayer = tagStack.peek();
    MarkupNode subtree = subtreeParser.result;
    subtreeParser = null;

    if (name.equals("for-separator") && currentLayer.forIterable != null) {
      currentLayer.forSeparator = subtree;
      return;
    }

    if (name.startsWith("let-")) {
      assert name.length() > 4;

      String bindingName = name.substring(4);

      if (!isValidExpressionIdentifier(bindingName))
        throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_IDENTIFIER, bindingName);

      if (bindingName.equals(currentLayer.forIterationVariable))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE, bindingName);

      LetBinding binding = new MarkupLetBinding(subtree, bindingName, lastPosition);

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE, bindingName);

      return;
    }

    assert tagAttributeBeginPosition != null;

    currentLayer.attributeMap.add(new MarkupAttribute(tagAttributeBeginPosition, name, subtree));
  }

  @Override
  public void onFlagAttribute(String name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '*') {
      handleStructuralAttribute(name, null, null);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();

    currentLayer.attributeMap.add(
      new ExpressionAttribute(
        lastPosition,
        name,
        ImmediateExpression.of(true),
        ExpressionFlag.IMMEDIATE_VALUE
      )
    );
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
    MarkupParser parser = new MarkupParser(tagRegistry);

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

  private void handleStructuralAttribute(String name, @Nullable String value, CursorPosition valueBeginPosition) {
    TagAndBuffers currentLayer = tagStack.peek();

    switch (name) {
      case "*if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = parseExpression(value, valueBeginPosition);
        currentLayer.ifConditionType = ConditionType.IF;
        return;
      }

      case "*else-if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = parseExpression(value, valueBeginPosition);
        currentLayer.ifConditionType = ConditionType.ELSE_IF;
        return;
      }

      case "*use": {
        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.useCondition != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_USE_CONDITIONS);

        currentLayer.useCondition = parseExpression(value, valueBeginPosition);
        return;
      }

      case "*other": {
        if (value != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG, name);

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.isWhenOther || currentLayer.whenIsValue != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.isWhenOther = true;
        return;
      }

      case "*else": {
        if (value != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG, name);

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifConditionType = ConditionType.ELSE;
        return;
      }

      case "*when": {
        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.whenInput != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT);

        currentLayer.whenInput = parseExpression(value, valueBeginPosition);
        return;
      }

      case "*is": {
        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.IS_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.whenIsValue != null || currentLayer.isWhenOther)
          throw new MarkupParseException(lastPosition, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.whenIsValue = value;
        return;
      }
    }

    if (name.equals("*for") || name.startsWith("*for-")) {
      String iterationVariable = null;

      if (name.length() == 5)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNNAMED_FOR_LOOP);

      if (name.length() > 5) {
        iterationVariable = name.substring(5);

        if (!isValidExpressionIdentifier(iterationVariable))
          throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_IDENTIFIER, iterationVariable);
      }

      if (value == null)
        throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

      if (currentLayer.forIterable != null)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_LOOPS);

      if (currentLayer.hasLetBinding(iterationVariable))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE, iterationVariable);

      currentLayer.forIterable = parseExpression(value, valueBeginPosition);
      currentLayer.forIterationVariable = iterationVariable;
      return;
    }

    throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_STRUCTURAL_ATTRIBUTE, name);
  }

  private void handleScalarNonStringAttribute(String name, ExpressionNode expression) {
    name = lower(name);

    if (name.charAt(0) == '*')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

    if (name.charAt(0) == '[')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    if (name.length() > 3 && name.charAt(0) == '.' && name.charAt(1) == '.' && name.charAt(2) == '.')
      throw new MarkupParseException(lastPosition, MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION, name.substring(3));

    if (name.equals("let") || name.startsWith("let-"))
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_NON_MARKUP_LET_ATTRIBUTE);

    TagAndBuffers currentLayer = tagStack.peek();

    if (currentLayer.forIterable != null) {
      if (name.equals("for-separator")) {
        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name, currentLayer.tagNameLower);

        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, name);
      }

      if (name.equals("for-reversed")) {
        if (currentLayer.forReversed != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name, currentLayer.tagNameLower);

        currentLayer.forReversed = expression;
        return;
      }
    }

    currentLayer.attributeMap.add(
      new ExpressionAttribute(
        lastPosition,
        name,
        expression,
        ExpressionFlag.IMMEDIATE_VALUE
      )
    );
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
      return ExpressionParser.parse(input);
    } catch (ExpressionTokenizeException expressionTokenizeException) {
      throw new MarkupParseException(valueBeginPosition, expressionTokenizeException);
    } catch (ExpressionParseException expressionParseException) {
      throw new MarkupParseException(valueBeginPosition, expressionParseException);
    }
  }
}
