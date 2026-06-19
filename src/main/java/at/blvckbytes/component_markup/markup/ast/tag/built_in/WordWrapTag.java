package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.BreakNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.parser.token.TokenEmitter;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class WordWrapTag extends TagDefinition {

  protected WordWrapTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("word-wrap", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @Nullable TokenEmitter tokenEmitter,
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode widthAttribute = attributes.getMandatoryExpressionNode("width");
    MarkupNode valueSeparator = attributes.getOptionalMarkupNode("value-separator");
    MarkupNode tokenRenderer = attributes.getOptionalMarkupNode("token-renderer");
    MarkupNode prefix = attributes.getOptionalMarkupNode("prefix");
    MarkupNode subsequentPrefix = attributes.getOptionalMarkupNode("subsequent-prefix");

    ExpressionList flagValueList = attributes.getOptionalBoundFlagExpressionList();
    ExpressionList valueList = flagValueList.isEmpty() ? attributes.getMandatoryExpressionList("value") : flagValueList;

    return new FunctionDrivenNode(tagName, letBindings, interpreter -> {
      List<String> values = new ArrayList<>();

      for (ExpressionNode value : valueList.get(interpreter))
        values.add(interpreter.evaluateAsString(value));

      if (tokenRenderer != null)
        interpreter.getEnvironment().beginScope();

      int maxWidth = (int) interpreter.evaluateAsLong(widthAttribute);
      int currentWidth = 0;

      if (prefix != null)
        currentWidth += interpretAndGetLength(interpreter, prefix);

      for (int valueIndex = 0; valueIndex < values.size(); ++valueIndex) {
        String[] tokens = values.get(valueIndex).split(" ");

        if (valueIndex != 0 && valueSeparator != null)
          currentWidth += interpretAndGetLength(interpreter, valueSeparator);

        for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex) {
          String token = tokens[tokenIndex];
          boolean prependSpace = tokenIndex != 0;

          if (prependSpace)
            ++currentWidth;

          // TODO: Break tokens that exceed the width-limit by using hyphens

          currentWidth += token.length();

          if (currentWidth > maxWidth) {
            prependSpace = false;
            currentWidth = token.length();

            if (currentWidth > 0)
              interpreter.interpret(new BreakNode(tagName));

            if (subsequentPrefix != null)
              currentWidth += interpretAndGetLength(interpreter, subsequentPrefix);
          }

          // The space is intentionally not prepended to the token, seeing how a token in this
          // context by definition does not contain spaces and doing so would be confusing, as
          // well as undesirable, as the renderer may use a regex of pattern ^...$.
          if (prependSpace)
            interpreter.interpret(new TextNode(tagName, " "));

          if (token.isEmpty())
            continue;

          if (tokenRenderer == null) {
            interpreter.interpret(new TextNode(tagName, token));
            continue;
          }

          interpreter.getEnvironment().setScopeVariable("token", token);
          interpreter.interpret(tokenRenderer);
        }
      }

      if (tokenRenderer != null)
        interpreter.getEnvironment().endScope();

      return null;
    });
  }

  private int interpretAndGetLength(Interpreter<?, ?> interpreter, MarkupNode node) {
    int lengthBefore = interpreter.getCurrentBuilder().getTotalTextLength();
    interpreter.interpret(node);
    int lengthAfter = interpreter.getCurrentBuilder().getTotalTextLength();
    return lengthAfter - lengthBefore;
  }
}
