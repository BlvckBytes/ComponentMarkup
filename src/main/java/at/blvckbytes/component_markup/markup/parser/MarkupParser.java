package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.InterpolationNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class MarkupParser implements XmlEventConsumer {

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

    AttributeName attributeName = AttributeName.parse(name, tokenOutput, false);

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      handleIntrinsicAttribute(attributeName, value, null);
      return;
    }

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attributeName, parseExpression(value), null);
      return;
    }

    handleUserAttribute(attributeName, value, value);
  }

  @Override
  public void onLongAttribute(StringView name, StringView raw, long value) {
    if (subtreeParser != null) {
      subtreeParser.onLongAttribute(name, raw, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput, false);

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      handleIntrinsicAttribute(attributeName, raw, null);
      return;
    }

    ExpressionNode immediateExpression = ImmediateExpression.ofLong(raw, value);

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attributeName, immediateExpression, value);
      return;
    }

    handleUserAttribute(attributeName, immediateExpression, raw);
  }

  @Override
  public void onDoubleAttribute(StringView name, StringView raw, double value) {
    if (subtreeParser != null) {
      subtreeParser.onDoubleAttribute(name, raw, value);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput, false);

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      handleIntrinsicAttribute(attributeName, raw, value);
      return;
    }

    ExpressionNode immediateExpression = ImmediateExpression.ofDouble(raw, value);

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attributeName, immediateExpression, value);
      return;
    }

    handleUserAttribute(attributeName, immediateExpression, raw);
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

    AttributeName attributeName = AttributeName.parse(name, tokenOutput, false);

    MarkupNode subtree = subtreeParser.result;
    subtreeParser = null;

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attributeName, subtree, null);
      return;
    }

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL))
      throw new MarkupParseException(name, MarkupParseError.LITERAL_INTRINSIC_MARKUP_ATTRIBUTE);

    handleUserAttribute(attributeName, subtree, null);
  }

  @Override
  public void onFlagAttribute(StringView name) {
    if (subtreeParser != null) {
      subtreeParser.onFlagAttribute(name);
      return;
    }

    AttributeName attributeName = AttributeName.parse(name, tokenOutput, true);
    ExpressionNode value = ImmediateExpression.ofBoolean(attributeName.finalName, !attributeName.has(AttributeFlag.FLAG_NEGATION));

    if (attributeName.has(AttributeFlag.INTRINSIC_EXPRESSION)) {
      handleIntrinsicAttribute(attributeName, null, value);
      return;
    }

    if (attributeName.has(AttributeFlag.INTRINSIC_LITERAL)) {
      handleIntrinsicAttribute(attributeName, null, value);
      return;
    }

    handleUserAttribute(attributeName, value, null);
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
  public void onInterpolation(StringView expression) {
    if (subtreeParser != null) {
      subtreeParser.onInterpolation(expression);
      return;
    }

    tagStack.peek().addChild(new InterpolationNode(expression, parseExpression(expression)));
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

  /**
   * @param value null => flag; String => literal (+); MarkupNode | ExpressionNode => evaluated (*)
   */
  private void handleIntrinsicAttribute(
    AttributeName attributeName,
    @Nullable Object value,
    @Nullable Object immediateValue
  ) {
    TagAndBuffers currentLayer = tagStack.peek();

    if (handleStaticallyNamedIntrinsicAttribute(attributeName, value, immediateValue)) {
      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_INTRINSIC, attributeName.finalName);

      return;
    }

    if (attributeName.finalName.contentEquals("for", true) || attributeName.finalName.startsWith("for-", true)) {
      if (!(value instanceof ExpressionNode) || immediateValue != null)
        throw new MarkupParseException(attributeName.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, attributeName.fullName.buildString());

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

      currentLayer.forAttributeName = attributeName.fullName;
      currentLayer.forIterable = (ExpressionNode) value;
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

      if (value instanceof StringView) {
        StringView stringView = (StringView) value;
        value = ImmediateExpression.ofString(stringView, stringView.buildString());
      }

      LetBinding binding;

      if (value instanceof MarkupNode)
        binding = new MarkupLetBinding((MarkupNode) value, bindingName, isCaptureMode);
      else {
        if (isCaptureMode && immediateValue != null)
          throw new MarkupParseException(attributeName.fullName, MarkupParseError.NON_MARKUP_OR_EXPRESSION_CAPTURE, bindingName.buildString());

        if (value instanceof ExpressionNode)
          binding = new ExpressionLetBinding((ExpressionNode) value, isCaptureMode, bindingName);
        else
          throw new MarkupParseException(attributeName.fullName, MarkupParseError.VALUELESS_BINDING, bindingName.buildString());
      }

      if (!currentLayer.addLetBinding(binding))
        throw new MarkupParseException(bindingName, MarkupParseError.BINDING_IN_USE, bindingName.buildString());

      return;
    }

    throw new MarkupParseException(attributeName.fullName, MarkupParseError.UNKNOWN_INTRINSIC_ATTRIBUTE, attributeName.fullName.buildString());
  }

  private boolean handleStaticallyNamedIntrinsicAttribute(
    AttributeName name,
    @Nullable Object value,
    @Nullable Object immediateValue
  ) {
    TagAndBuffers currentLayer = tagStack.peek();

    assert currentLayer.tagName != null;

    switch (name.finalName.buildString()) {
      case "if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = (ExpressionNode) value;
        currentLayer.ifConditionType = ConditionType.IF;
        return true;
      }

      case "else-if": {
        if (currentLayer.parent != null && currentLayer.parent.whenInput != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER);

        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifCondition = (ExpressionNode) value;
        currentLayer.ifConditionType = ConditionType.ELSE_IF;
        return true;
      }

      case "use": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.useCondition != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_USE_CONDITIONS);

        currentLayer.useCondition = (ExpressionNode) value;
        return true;
      }

      case "other": {
        if (value != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name.fullName.buildString());

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.isWhenOther || currentLayer.whenIsValue != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.isWhenOther = true;
        return true;
      }

      case "else": {
        if (value != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG, name.fullName.buildString());

        if (currentLayer.ifConditionType != ConditionType.NONE)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS);

        currentLayer.ifConditionType = ConditionType.ELSE;
        return true;
      }

      case "when": {
        if (!(value instanceof ExpressionNode) || immediateValue != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.whenInput != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT);

        currentLayer.whenInput = (ExpressionNode) value;
        return true;
      }

      case "is": {
        StringView isValue;

        if (value instanceof StringView)
          isValue = ((StringView) value);
        else
          throw new MarkupParseException(name.fullName, MarkupParseError.NON_LITERAL_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.parent == null || currentLayer.parent.whenInput == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.IS_CASE_OUTSIDE_OF_WHEN_PARENT);

        if (currentLayer.whenIsValue != null || currentLayer.isWhenOther)
          throw new MarkupParseException(name.fullName, MarkupParseError.WHEN_MATCHING_COLLIDING_CASES);

        currentLayer.whenIsValue = isValue;
        return true;
      }

      case "for-reversed":
        if (!(value instanceof ExpressionNode)) {
          if (value != null)
            throw new MarkupParseException(name.fullName, MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

          if (immediateValue == null)
            throw new IllegalStateException("Expected an immediate value to have been provided on null-value (flag)");

          value = immediateValue;
        }

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forReversed != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name.fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forReversed = (ExpressionNode) value;
        return true;

      case "for-empty":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, name.fullName.buildString());

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forEmpty != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name.fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forEmpty = (MarkupNode) value;
        return true;

      case "for-separator":
        if (!(value instanceof MarkupNode))
          throw new MarkupParseException(name.fullName, MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE, name.fullName.buildString());

        if (currentLayer.forIterable == null)
          throw new MarkupParseException(name.fullName, MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE, name.fullName.buildString());

        if (currentLayer.forSeparator != null)
          throw new MarkupParseException(name.fullName, MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE, name.fullName.buildString(), currentLayer.tagName.buildString());

        currentLayer.forSeparator = (MarkupNode) value;
        return true;

      case "for-":
        throw new MarkupParseException(name.fullName, MarkupParseError.UNNAMED_FOR_LOOP);

      case "let":
      case "let-":
        throw new MarkupParseException(name.fullName, MarkupParseError.UNNAMED_LET_BINDING);
    }

    return false;
  }

  private void handleUserAttribute(AttributeName attributeName, @NotNull Object value, @Nullable StringView rawValue) {
    if (isInvalidIdentifier(attributeName.finalName, false))
      throw new MarkupParseException(attributeName.finalName, MarkupParseError.MALFORMED_ATTRIBUTE_NAME, attributeName.finalName.buildString());

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__ATTRIBUTE_USER, attributeName.finalName);

    TagAndBuffers currentLayer = tagStack.peek();

    assert currentLayer.attributeMap != null;

    ExpressionNode expression;

    if (attributeName.has(AttributeFlag.BINDING_MODE)) {
      if (!(value instanceof StringView))
        throw new MarkupParseException(attributeName.finalName, MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE);

      expression = parseExpression((StringView) value);
    }

    else if (value instanceof MarkupNode) {
      currentLayer.attributeMap.add(new MarkupAttribute(attributeName, (MarkupNode) value));
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

    currentLayer.attributeMap.add(new ExpressionAttribute(attributeName, expression));
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
        throw new MarkupParseException(value, MarkupParseError.EMPTY_EXPRESSION);

      return expression;
    } catch (ExpressionTokenizeException expressionTokenizeException) {
      throw new MarkupParseException(value.startInclusive, expressionTokenizeException);
    } catch (ExpressionParseException expressionParseException) {
      throw new MarkupParseException(value.startInclusive, expressionParseException);
    }
  }
}
