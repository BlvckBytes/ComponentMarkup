package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.ast.tag.attribute.SubtreeAttribute;
import at.blvckbytes.component_markup.ast.tag.built_in.ContainerTag;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.expression.tokenizer.token.BooleanToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.DoubleToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.LongToken;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.XmlEventConsumer;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import at.blvckbytes.component_markup.xml.XmlParseException;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

  private static final ExpressionNode EMPTY_TEXT = ImmediateExpression.of("");

  private final TagRegistry tagRegistry;
  private final Stack<TagAndBuffers> tagStack;

  private CursorPosition lastPosition;
  private @Nullable MarkupParser subtreeParser;
  private MarkupNode result;

  private MarkupParser(TagRegistry tagRegistry) {
    this(tagRegistry, CursorPosition.ZERO);
  }

  private MarkupParser(TagRegistry tagRegistry, CursorPosition initialPosition) {
    this.tagRegistry = tagRegistry;
    this.tagStack = new Stack<>();
    this.lastPosition = initialPosition;
    this.result = new TextNode(EMPTY_TEXT, lastPosition, null);

    this.tagStack.push(new TagAndBuffers(ContainerTag.INSTANCE, ContainerTag.TAG_NAME, lastPosition));
  }

  // ================================================================================
  // XML Event-Consumer
  // ================================================================================

  @Override
  public void onCursorPosition(CursorPosition position) {
    if (subtreeParser != null)
      subtreeParser.lastPosition = position;

    this.lastPosition = position;
  }

  @Override
  public void onTagOpenBegin(String tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagOpenBegin(tagName);
      return;
    }

    String tagNameLower = lower(tagName);
    TagDefinition tag = tagRegistry.locateTag(tagNameLower);

    if (tag == null)
      throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG);

    tagStack.push(new TagAndBuffers(tag, tagNameLower, lastPosition));
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
        throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_IDENTIFIER);

      if (bindingName.equals(currentLayer.forIterationVariable))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE);

      ExpressionNode bindingExpression = parseExpression(value, valueBeginPosition);
      LetBinding binding = new LetBinding(bindingName, bindingExpression, lastPosition);

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE);

      return;
    }

    boolean isExpressionMode = false;

    if (nameLength > 2 && name.charAt(0) == '[') {
      if (name.charAt(nameLength - 1) != ']')
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

      isExpressionMode = true;
      name = name.substring(1, nameLength - 1);
    }

    ExpressionNode expression = isExpressionMode ? parseExpression(value, valueBeginPosition) : ImmediateExpression.of(value);

    if (currentLayer.forIterable != null) {
      if (name.equals("for-reversed")) {
        if (currentLayer.forReversed != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

        currentLayer.forReversed = expression;
        return;
      }

      if (name.equals("for-separator")) {
        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SUBTREE_VALUE);
      }
    }

    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_ATTRIBUTE);

    if (attribute.type == AttributeType.SUBTREE)
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SUBTREE_VALUE);

    if (!attribute.multiValue && currentLayer.hasAttribute(name))
      throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

    currentLayer.addAttribute(new ExpressionAttribute(name, lastPosition, expression));
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

    name = lower(name);

    if (name.charAt(0) == '*')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

    if (name.charAt(0) == '[')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    if (name.equals("let") || name.startsWith("let-"))
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_LET_ATTRIBUTE);

    TagAndBuffers currentLayer = tagStack.peek();

    if (name.equals("for-separator") && currentLayer.forIterable != null) {
      if (currentLayer.forSeparator != null)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);
    }

    else if (name.equals("for-reversed"))
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SCALAR_VALUE);

    else {
      AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

      if (attribute == null)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_ATTRIBUTE);

      if (!attribute.multiValue && currentLayer.hasAttribute(name))
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

      if (attribute.type != AttributeType.SUBTREE)
        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SCALAR_VALUE);
    }

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

    currentLayer.addAttribute(new SubtreeAttribute(name, subtree));
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

    throw new MarkupParseException(lastPosition, MarkupParseError.MISSING_ATTRIBUTE_VALUE);
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
        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SELF_CLOSING_TAG);

      return;
    }

    if (tagClosing == TagClosing.OPEN_CLOSE)
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_OPEN_CLOSE_TAG);

    tagStack.pop();
    tagStack.peek().children.add(currentLayer);
  }

  @Override
  public void onText(String text) {
    if (subtreeParser != null) {
      subtreeParser.onText(text);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.children.add(new TextNode(ImmediateExpression.of(text), lastPosition, null));
  }

  @Override
  public void onInterpolation(String expression, CursorPosition valueBeginPosition) {
    if (subtreeParser != null) {
      subtreeParser.onInterpolation(expression, valueBeginPosition);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.children.add(new TextNode(parseExpression(expression, valueBeginPosition), lastPosition, null));
  }

  @Override
  public void onTagClose(String tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagClose(tagName);
      return;
    }

    tagName = lower(tagName);

    TagDefinition closedTag = tagRegistry.locateTag(tagName);

    if (closedTag == null)
      throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_TAG);

    TagAndBuffers openedTag;

    do {
      openedTag = tagStack.pop();

      if (tagStack.isEmpty())
        throw new MarkupParseException(lastPosition, MarkupParseError.UNBALANCED_CLOSING_TAG);

      tagStack.peek().children.add(openedTag);
    } while(!openedTag.tagNameLower.equals(tagName));
  }

  @Override
  public void onInputEnd() {
    if (subtreeParser != null)
      subtreeParser.onInputEnd();

    while (true) {
      TagAndBuffers currentLayer = tagStack.pop();

      if (tagStack.isEmpty()) {
        this.result = currentLayer.construct();
        break;
      }

      tagStack.peek().children.add(currentLayer);
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
        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.conditionType != ConditionType.NONE)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_CONDITIONS);

        currentLayer.condition = parseExpression(value, valueBeginPosition);
        currentLayer.conditionType = ConditionType.IF;
        return;
      }

      case "*else-if": {
        if (value == null)
          throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

        if (currentLayer.conditionType != ConditionType.NONE)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_CONDITIONS);

        currentLayer.condition = parseExpression(value, valueBeginPosition);
        currentLayer.conditionType = ConditionType.ELSE_IF;
        return;
      }

      case "*else": {
        if (value != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG);

        if (currentLayer.conditionType != ConditionType.NONE)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_CONDITIONS);

        currentLayer.conditionType = ConditionType.ELSE;
        return;
      }

      case "*for":
      case "*for-":
        throw new MarkupParseException(lastPosition, MarkupParseError.UNNAMED_FOR_LOOP);
    }

    if (name.startsWith("*for-")) {
      if (name.length() == 5)
        throw new MarkupParseException(lastPosition, MarkupParseError.UNNAMED_FOR_LOOP);

      if (value == null)
        throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

      if (currentLayer.forIterable != null)
        throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_LOOPS);

      String iterationVariable = name.substring(5);

      if (!isValidExpressionIdentifier(iterationVariable))
        throw new MarkupParseException(lastPosition, MarkupParseError.MALFORMED_IDENTIFIER);

      if (currentLayer.hasLetBinding(iterationVariable))
        throw new MarkupParseException(lastPosition, MarkupParseError.BINDING_IN_USE);

      currentLayer.forIterable = parseExpression(value, valueBeginPosition);
      currentLayer.forIterationVariable = iterationVariable;
      return;
    }

    throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_STRUCTURAL_ATTRIBUTE);
  }

  private void handleScalarNonStringAttribute(String name, ExpressionNode expression) {
    name = lower(name);

    if (name.charAt(0) == '*')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

    if (name.charAt(0) == '[')
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    if (name.equals("let") || name.startsWith("let-"))
      throw new MarkupParseException(lastPosition, MarkupParseError.NON_STRING_LET_ATTRIBUTE);

    TagAndBuffers currentLayer = tagStack.peek();

    if (currentLayer.forIterable != null) {
      if (name.equals("for-separator")) {
        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

        throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SUBTREE_VALUE);
      }

      if (name.equals("for-reversed")) {
        if (currentLayer.forReversed != null)
          throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

        currentLayer.forReversed = expression;
        return;
      }
    }

    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new MarkupParseException(lastPosition, MarkupParseError.UNKNOWN_ATTRIBUTE);

    if (attribute.type == AttributeType.SUBTREE)
      throw new MarkupParseException(lastPosition, MarkupParseError.EXPECTED_SUBTREE_VALUE);

    if (!attribute.multiValue && currentLayer.hasAttribute(name))
      throw new MarkupParseException(lastPosition, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);

    currentLayer.addAttribute(new ExpressionAttribute(name, lastPosition, expression));
  }

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
