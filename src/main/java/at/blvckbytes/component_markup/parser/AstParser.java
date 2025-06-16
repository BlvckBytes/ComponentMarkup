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
    this(tagRegistry, expressionEvaluator, new CursorPosition(0, 0, 0));
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
      throw new IllegalStateException("Unknown tag: " + tagName);

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
        throw new IllegalStateException("Use let-<name>; unnamed bindings are not supported");

      String bindingName = name.substring(4);
      AExpression bindingExpression = expressionEvaluator.parseString(value);
      currentLayer.bindings.add(new LetBinding(bindingName, bindingExpression, lastPosition));
      return;
    }

    boolean isExpressionMode = false;

    if (nameLength > 2 && name.charAt(0) == '[') {
      if (name.charAt(nameLength - 1) != ']')
        throw new IllegalStateException("Unbalanced attribute-name brackets");

      isExpressionMode = true;
      name = name.substring(1, nameLength - 1);
    }

    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new IllegalStateException("Unsupported attribute: " + name);

    if (attribute.type == AttributeType.SUBTREE)
      throw new IllegalStateException("The attribute " + name + " expected a tag-value");

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
      throw new IllegalStateException("Structural attributes only support string-values");

    if (name.charAt(0) == '[')
      throw new IllegalStateException("Only string-values support expression-mode");

    TagAndBuffers currentLayer = tagStack.peek();
    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new IllegalStateException("Unsupported attribute: " + name);

    if (attribute.type != AttributeType.SUBTREE)
      throw new IllegalStateException("The attribute " + name + " expected a scalar value");

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
    currentLayer.attributes.add(new SubtreeAttribute(name, subtreeParser.getResult()));
    subtreeParser = null;
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
      throw new IllegalStateException("Only string-values support expression-mode");

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
        throw new IllegalStateException("The tag " + tagName + " requires self-closing");

      return;
    }

    if (tagClosing == TagClosing.OPEN_CLOSE)
      throw new IllegalStateException("The tag " + tagName + " requires content");

    AstNode node = tagStack.pop().construct();
    tagStack.peek().children.add(node);
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
      throw new IllegalStateException("Unknown tag: " + tagName);

    TagAndBuffers openedTag;

    do {
      if (tagStack.isEmpty())
        throw new IllegalStateException("The tag " + tagName + " has not been opened before");

      openedTag = tagStack.pop();
      tagStack.peek().children.add(openedTag.construct());
    } while(openedTag.tag != closedTag);
  }

  @Override
  public void onInputEnd() {
    if (subtreeParser != null)
      subtreeParser.onInputEnd();

    while (true) {
      AstNode node = tagStack.pop().construct();

      if (tagStack.isEmpty()) {
        this.result = node;
        break;
      }

      tagStack.peek().children.add(node);
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
    switch (name) {
      case "*if": {
        if (value == null)
          throw new IllegalStateException("The *if attribute requires a value representing the condition");

        AExpression condition = expressionEvaluator.parseString(value);
        throw new UnsupportedOperationException(); // TODO
      }

      case "*else-if": {
        if (value == null)
          throw new IllegalStateException("The *else-if attribute requires a value representing the condition");

        AExpression condition = expressionEvaluator.parseString(value);
        throw new UnsupportedOperationException(); // TODO
      }

      case "*else":
        if (value != null)
          throw new IllegalStateException("The *else attribute does not support a value");

        throw new UnsupportedOperationException(); // TODO
      case "*for":
        throw new IllegalStateException("Use *for-<name>; unnamed loops are not supported");
    }

    if (name.startsWith("*for-")) {
      if (name.length() == 5)
        throw new IllegalStateException("Use *for-<name>; unnamed loops are not supported");

      if (value == null)
        throw new IllegalStateException("The *for- attribute requires a value representing the iterable");

      String variableName = name.substring(5);
      AExpression iterable = expressionEvaluator.parseString(value);
      throw new UnsupportedOperationException(); // TODO
    }

    throw new IllegalStateException("Unknown structural attribute: " + name);
  }

  private void handleScalarNonStringAttribute(String name, AExpression expression) {
    name = lower(name);

    if (name.charAt(0) == '*')
      throw new IllegalStateException("Structural attributes only support string-values");

    if (name.charAt(0) == '[')
      throw new IllegalStateException("Only string-values support expression-mode");

    if (name.startsWith("let-"))
      throw new IllegalStateException("Only string-values (expressions) support let-binding");

    TagAndBuffers currentLayer = tagStack.peek();
    AttributeDefinition attribute = currentLayer.tag.getAttribute(name);

    if (attribute == null)
      throw new IllegalStateException("Unsupported attribute: " + name);

    if (attribute.type == AttributeType.SUBTREE)
      throw new IllegalStateException("The attribute " + name + " expected a tag-value");

    currentLayer.attributes.add(new ExpressionAttribute(name, lastPosition, expression));
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
