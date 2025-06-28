package at.blvckbytes.component_markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.interpreter.*;
import at.blvckbytes.component_markup.util.JsonifyIgnore;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

public class GradientNode extends AstNode implements InterpreterInterceptor {

  @JsonifyIgnore
  private final ThreadLocal<Stack<List<Object>>> threadLocalInjectedComponentsStack = ThreadLocal.withInitial(Stack::new);

  private final boolean affectSubtrees;

  public GradientNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    // TODO: apply if requested
    this.affectSubtrees = false;
  }

  @Override
  public EnumSet<InterceptionFlag> interceptInterpretation(AstNode node, Interpreter interpreter) {
    if (!affectSubtrees && interpreter.isInSubtree())
      return EnumSet.noneOf(InterceptionFlag.class);

    if (node instanceof GradientNode) {
      threadLocalInjectedComponentsStack.get().push(new ArrayList<>());

      return EnumSet.of(InterceptionFlag.CALL_AFTER);
    }

    List<Object> injectedNodes = threadLocalInjectedComponentsStack.get().peek();

    if (node instanceof ContentNode) {
      OutputBuilder builder = interpreter.getCurrentBuilder();
      NodeStyle nodeStyle = ((ContentNode) node).getStyle();

      if (nodeStyle != null) {
        if (nodeStyle.color != null && interpreter.evaluateAsBooleanOrNull(nodeStyle.color) != null)
          return EnumSet.noneOf(InterceptionFlag.class);
      }

      if (node instanceof TextNode) {
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

        return EnumSet.of(InterceptionFlag.SKIP_PROCESSING);
      }

      Object otherComponent = builder.onContent((ContentNode) node);
      injectedNodes.add(otherComponent);

      return EnumSet.of(InterceptionFlag.SKIP_PROCESSING);
    }

    return EnumSet.noneOf(InterceptionFlag.class);
  }

  @Override
  public void afterInterpretation(AstNode node, Interpreter interpreter) {
    if (node instanceof GradientNode) {
      List<Object> injectedComponents = threadLocalInjectedComponentsStack.get().pop();

      int injectedComponentsCount = injectedComponents.size();

      for (int injectedComponentsIndex = 0; injectedComponentsIndex < injectedComponentsCount; ++injectedComponentsIndex) {
        Object injectedComponent = injectedComponents.get(injectedComponentsIndex);

        // TODO: Compute color based on progression as well as start, end and intermediate colors
        double gradientProgression = (injectedComponentsIndex + 1D) / injectedComponentsCount;
        String hexColor = "#000000";

        interpreter.getComponentConstructor().setColor(injectedComponent, hexColor);
      }
    }
  }

  @Override
  public void onSkippedByOther(AstNode node, Interpreter interpreter) {
    if (!affectSubtrees && interpreter.isInSubtree())
      return;

    if (node instanceof GradientNode)
      threadLocalInjectedComponentsStack.get().pop();
  }
}
