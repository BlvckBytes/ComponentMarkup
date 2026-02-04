package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.BreakNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
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
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionNode widthAttribute = attributes.getMandatoryExpressionNode("width");
    MarkupNode valueSeparator = attributes.getOptionalMarkupNode("value-separator");
    MarkupNode tokenRenderer = attributes.getOptionalMarkupNode("token-renderer");

    ExpressionList flagValueList = attributes.getOptionalBoundFlagExpressionList();
    ExpressionList valueList = flagValueList.isEmpty() ? attributes.getMandatoryExpressionList("value") : flagValueList;

    return new FunctionDrivenNode(tagName, interpreter -> {
      List<String> values = new ArrayList<>();

      for (ExpressionNode value : valueList.get(interpreter))
        values.add(interpreter.evaluateAsString(value));

      if (tokenRenderer != null)
        interpreter.getEnvironment().beginScope();

      int maxWidth = (int) interpreter.evaluateAsLong(widthAttribute);
      int currentWidth = 0;

      for (int valueIndex = 0; valueIndex < values.size(); ++valueIndex) {
        String[] tokens = values.get(valueIndex).split(" ");

        if (valueIndex != 0 && valueSeparator != null) {
          int lengthBefore = interpreter.getCurrentBuilder().getTotalTextLength();
          interpreter.interpret(valueSeparator);
          int lengthAfter = interpreter.getCurrentBuilder().getTotalTextLength();
          currentWidth += lengthAfter - lengthBefore;
        }

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
          }

          // By prepending the space to the current token, we allow the output to buffer
          // texts of equal style, which will yield a more compact component-tree; This
          // shouldn't have any undesirable consequences - if so, we'll fix it as soon
          // as they become a noticeable issue.
          if (prependSpace)
            token = " " + token;

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
}
