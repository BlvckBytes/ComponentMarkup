package at.blvckbytes.component_markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.interpreter.*;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GradientNode extends AstNode implements InterpreterInterceptor {

  private final ThreadLocal<Stack<List<Object>>> threadLocalInjectedComponentsStack = ThreadLocal.withInitial(Stack::new);

  public GradientNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "GradientNode{\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }

  @Override
  public InterceptionResult interceptInterpretation(AstNode node, OutputBuilder builder, Interpreter interpreter) {
    if (node instanceof GradientNode) {
      threadLocalInjectedComponentsStack.get().push(new ArrayList<>());
      return InterceptionResult.PROCESS_DO_CALL_AFTER;
    }

    List<Object> injectedNodes = threadLocalInjectedComponentsStack.get().peek();

    if (node instanceof TextNode) {
      NodeStyle nodeStyle = ((TextNode) node).getStyle();

      if (nodeStyle != null && nodeStyle.color != ImmediateExpression.ofNull())
        return InterceptionResult.PROCESS_DO_NOT_CALL_AFTER;

      String nodeText = interpreter.evaluateAsString(((TextNode) node).text);
      StringBuilder whitespaceAccumulator = new StringBuilder();

      for (int charIndex = 0; charIndex < nodeText.length(); ++charIndex) {
        char currentChar = nodeText.charAt(charIndex);

        if (Character.isWhitespace(currentChar)) {
          whitespaceAccumulator.append(currentChar);
          continue;
        }

        if (whitespaceAccumulator.length() > 0) {
          builder.onContent(new TextNode(ImmediateExpression.of(whitespaceAccumulator.toString()), node.position, null));
          whitespaceAccumulator.setLength(0);
        }

        TextNode charNode = new TextNode(ImmediateExpression.of(String.valueOf(currentChar)), node.position, null);
        Object charComponent = builder.onContent(charNode);
        injectedNodes.add(charComponent);
      }

      if (whitespaceAccumulator.length() > 0)
        builder.onContent(new TextNode(ImmediateExpression.of(whitespaceAccumulator.toString()), node.position, null));

      return InterceptionResult.DO_NOT_PROCESS;
    }

    return InterceptionResult.PROCESS_DO_NOT_CALL_AFTER;
  }

  @Override
  public void afterInterpretation(AstNode node, OutputBuilder builder, Interpreter interpreter) {
    if (node instanceof GradientNode) {
      List<Object> injectedComponents = threadLocalInjectedComponentsStack.get().pop();

      int injectedComponentsCount = injectedComponents.size();

      for (int injectedComponentsIndex = 0; injectedComponentsIndex < injectedComponentsCount; ++injectedComponentsIndex) {
        Object injectedComponent = injectedComponents.get(injectedComponentsIndex);

        // TODO: Compute color based on progression as well as start, end and intermediate colors
        double gradientProgression = (injectedComponentsIndex + 1D) / injectedComponentsCount;
        String hexColor = "#000000";

        builder.componentConstructor.setColor(injectedComponent, hexColor);
      }
    }
  }
}
