package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.ast.tag.attribute.SubtreeAttribute;
import at.blvckbytes.component_markup.ast.tag.built_in.ContainerTag;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.XmlEventConsumer;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class AstParser implements XmlEventConsumer {

  private static final AExpression EMPTY_TEXT = ImmediateExpression.of("");

  private final TagRegistry tagRegistry;
  private final IExpressionEvaluator expressionEvaluator;
  private final Stack<TagAndBuffers> tagStack;

  private CursorPosition lastPosition;
  private @Nullable AstParser subtreeParser;
  private AstNode result;

  public AstParser(TagRegistry tagRegistry, IExpressionEvaluator expressionEvaluator) {
    this(tagRegistry, expressionEvaluator, CursorPosition.ZERO);
  }

  private AstParser(TagRegistry tagRegistry, IExpressionEvaluator expressionEvaluator, CursorPosition initialPosition) {
    this.expressionEvaluator = expressionEvaluator;
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
      throw new AstParseException(lastPosition, AstParseError.UNKNOWN_TAG);

    tagStack.push(new TagAndBuffers(tag, tagNameLower, lastPosition));
  }

  @Override
  public void onStringAttribute(String name, String value) {
    if (subtreeParser != null) {
      subtreeParser.onStringAttribute(name, value);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '*') {
      handleStructuralAttribute(name, value);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();

    int nameLength = name.length();

    if (name.startsWith("let-")) {
      if (nameLength == 4)
        throw new AstParseException(lastPosition, AstParseError.UNNAMED_LET_BINDING);

      String bindingName = name.substring(4);

      if (!isValidExpressionIdentifier(bindingName))
        throw new AstParseException(lastPosition, AstParseError.MALFORMED_IDENTIFIER);

      if (bindingName.equals(currentLayer.iterationVariable))
        throw new AstParseException(lastPosition, AstParseError.BINDING_IN_USE);

      AExpression bindingExpression = expressionEvaluator.parseString(value);
      LetBinding binding = new LetBinding(bindingName, bindingExpression, lastPosition);

      if (!currentLayer.addLetBinding(binding))
        throw new AstParseException(lastPosition, AstParseError.BINDING_IN_USE);

      return;
    }

    boolean isExpressionMode = false;

    if (nameLength > 2 && name.charAt(0) == '[') {
      if (name.charAt(nameLength - 1) != ']')
        throw new AstParseException(lastPosition, AstParseError.UNBALANCED_ATTRIBUTE_BRACKETS);

      isExpressionMode = true;
      name = name.substring(1, nameLength - 1);
    }

    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new AstParseException(lastPosition, AstParseError.UNKNOWN_ATTRIBUTE);

    if (attribute.type == AttributeType.SUBTREE)
      throw new AstParseException(lastPosition, AstParseError.EXPECTED_SUBTREE_VALUE);

    AExpression expression = isExpressionMode ? expressionEvaluator.parseString(value) : ImmediateExpression.of(value);
    currentLayer.attributes.add(new ExpressionAttribute(name, lastPosition, expression));
  }

  @Override
  public void onLongAttribute(String name, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, value);
      return;
    }

    handleScalarNonStringAttribute(name, ImmediateExpression.of(value));
  }

  @Override
  public void onDoubleAttribute(String name, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, value);
      return;
    }

    handleScalarNonStringAttribute(name, ImmediateExpression.of(value));
  }

  @Override
  public void onBooleanAttribute(String name, boolean value) {
    if (subtreeParser != null) {
      subtreeParser.onBooleanAttribute(name, value);
      return;
    }

    handleScalarNonStringAttribute(name, ImmediateExpression.of(value));
  }

  @Override
  public void onTagAttributeBegin(String name) {
    if (subtreeParser != null) {
      subtreeParser.onTagAttributeBegin(name);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '*')
      throw new AstParseException(lastPosition, AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

    if (name.charAt(0) == '[')
      throw new AstParseException(lastPosition, AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    TagAndBuffers currentLayer = tagStack.peek();

    if (name.equals("for-separator") && currentLayer.iterable != null) {
      if (currentLayer.separator != null)
        throw new AstParseException(lastPosition, AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE);
    }

    else {
      AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

      if (attribute == null)
        throw new AstParseException(lastPosition, AstParseError.UNKNOWN_ATTRIBUTE);

      if (attribute.type != AttributeType.SUBTREE)
        throw new AstParseException(lastPosition, AstParseError.EXPECTED_SCALAR_VALUE);
    }

    subtreeParser = new AstParser(tagRegistry, expressionEvaluator, lastPosition);
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

    TagAndBuffers currentLayer = tagStack.peek();
    AstNode subtree = subtreeParser.getResult();
    subtreeParser = null;

    if (name.equals("for-separator") && currentLayer.iterable != null) {
      currentLayer.separator = subtree;
      return;
    }

    currentLayer.attributes.add(new SubtreeAttribute(name, subtree));
  }

  @Override
  public void onFlagAttribute(String name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    name = lower(name);

    if (name.charAt(0) == '*') {
      handleStructuralAttribute(name, null);
      return;
    }

    if (name.charAt(0) == '[')
      throw new AstParseException(lastPosition, AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    handleScalarNonStringAttribute(name, ImmediateExpression.of(true));
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
        throw new AstParseException(lastPosition, AstParseError.EXPECTED_SELF_CLOSING_TAG);

      return;
    }

    if (tagClosing == TagClosing.OPEN_CLOSE)
      throw new AstParseException(lastPosition, AstParseError.EXPECTED_OPEN_CLOSE_TAG);

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
  public void onInterpolation(String expression) {
    if (subtreeParser != null) {
      subtreeParser.onInterpolation(expression);
      return;
    }

    TagAndBuffers currentLayer = tagStack.peek();
    currentLayer.children.add(new TextNode(expressionEvaluator.parseString(expression), lastPosition, null));
  }

  @Override
  public void onTagClose(String tagName) {
    if (subtreeParser != null) {
      subtreeParser.onTagClose(tagName);
      return;
    }

    TagDefinition closedTag = tagRegistry.locateTag(lower(tagName));

    if (closedTag == null)
      throw new AstParseException(lastPosition, AstParseError.UNKNOWN_TAG);

    TagAndBuffers openedTag;

    do {
      if (tagStack.isEmpty())
        throw new AstParseException(lastPosition, AstParseError.UNBALANCED_CLOSING_TAG);

      openedTag = tagStack.pop();
      tagStack.peek().children.add(openedTag);
    } while(openedTag.tag != closedTag);
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

  public AstNode getResult() {
    return result;
  }

  // ================================================================================
  // Utilities
  // ================================================================================

  private void handleStructuralAttribute(String name, @Nullable String value) {
    TagAndBuffers currentLayer = tagStack.peek();

    switch (name) {
      case "*if": {
        if (value == null)
          throw new AstParseException(lastPosition, AstParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_VALUE);

        if (currentLayer.conditionType != ConditionType.NONE)
          throw new AstParseException(lastPosition, AstParseError.MULTIPLE_CONDITIONS);

        currentLayer.condition = expressionEvaluator.parseString(value);
        currentLayer.conditionType = ConditionType.IF;
        return;
      }

      case "*else-if": {
        if (value == null)
          throw new AstParseException(lastPosition, AstParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_VALUE);

        if (currentLayer.conditionType != ConditionType.NONE)
          throw new AstParseException(lastPosition, AstParseError.MULTIPLE_CONDITIONS);

        currentLayer.condition = expressionEvaluator.parseString(value);
        currentLayer.conditionType = ConditionType.ELSE_IF;
        return;
      }

      case "*else": {
        if (value != null)
          throw new AstParseException(lastPosition, AstParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG);

        if (currentLayer.conditionType != ConditionType.NONE)
          throw new AstParseException(lastPosition, AstParseError.MULTIPLE_CONDITIONS);

        currentLayer.conditionType = ConditionType.ELSE;
        return;
      }

      case "*for":
        throw new AstParseException(lastPosition, AstParseError.UNNAMED_FOR_LOOP);
    }

    if (name.startsWith("*for-")) {
      if (name.length() == 5)
        throw new AstParseException(lastPosition, AstParseError.UNNAMED_FOR_LOOP);

      if (value == null)
        throw new AstParseException(lastPosition, AstParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_VALUE);

      if (currentLayer.iterable != null)
        throw new AstParseException(lastPosition, AstParseError.MULTIPLE_LOOPS);

      String iterationVariable = name.substring(5);

      if (!isValidExpressionIdentifier(iterationVariable))
        throw new AstParseException(lastPosition, AstParseError.MALFORMED_IDENTIFIER);

      if (currentLayer.hasLetBinding(iterationVariable))
        throw new AstParseException(lastPosition, AstParseError.BINDING_IN_USE);

      currentLayer.iterable = expressionEvaluator.parseString(value);
      currentLayer.iterationVariable = iterationVariable;
      return;
    }

    throw new AstParseException(lastPosition, AstParseError.UNKNOWN_STRUCTURAL_ATTRIBUTE);
  }

  private void handleScalarNonStringAttribute(String name, AExpression expression) {
    name = lower(name);

    if (name.charAt(0) == '*')
      throw new AstParseException(lastPosition, AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE);

    if (name.charAt(0) == '[')
      throw new AstParseException(lastPosition, AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

    if (name.startsWith("let-"))
      throw new AstParseException(lastPosition, AstParseError.NON_STRING_LET_ATTRIBUTE);

    TagAndBuffers currentLayer = tagStack.peek();
    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new AstParseException(lastPosition, AstParseError.UNKNOWN_ATTRIBUTE);

    if (attribute.type == AttributeType.SUBTREE)
      throw new AstParseException(lastPosition, AstParseError.EXPECTED_SUBTREE_VALUE);

    currentLayer.attributes.add(new ExpressionAttribute(name, lastPosition, expression));
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
}
