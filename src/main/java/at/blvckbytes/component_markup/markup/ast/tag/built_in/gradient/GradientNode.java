package at.blvckbytes.component_markup.markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.*;
import at.blvckbytes.component_markup.util.JsonifyIgnore;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradientNode extends MarkupNode implements InterpreterInterceptor {

  @JsonifyIgnore
  private final ThreadLocal<@Nullable GradientNodeState> threadLocalState = ThreadLocal.withInitial(() -> null);

  private final List<ExpressionNode> colors;
  private final List<ExpressionNode> offsets;
  private final @Nullable ExpressionNode deep;

  public GradientNode(
    List<ExpressionNode> colors,
    List<ExpressionNode> offsets,
    @Nullable ExpressionNode deep,
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.colors = colors;
    this.offsets = offsets;
    this.deep = deep;
  }

  private GradientNodeState getState(Interpreter interpreter) {
    GradientNodeState state;

    if ((state = threadLocalState.get()) != null)
      return state;

    state = new GradientNodeState(colors, offsets, deep, interpreter);

    threadLocalState.set(state);
    return state;
  }

  @Override
  public InterceptionResult interceptInterpretation(MarkupNode node, Interpreter interpreter) {
    GradientNodeState state = getState(interpreter);

    if (!state.deep && interpreter.isInSubtree())
      return InterceptionResult.DO_PROCESS;

    if (node instanceof GradientNode) {
      state.injectedComponentsStack.push(new ArrayList<>());
      return InterceptionResult.DO_PROCESS_AND_CALL_AFTER;
    }

    List<Object> injectedNodes = state.injectedComponentsStack.peek();

    if (node instanceof ContentNode) {
      OutputBuilder builder = interpreter.getCurrentBuilder();
      NodeStyle nodeStyle = ((ContentNode) node).getStyle();

      if (nodeStyle != null) {
        // Do not override established colors!
        // TODO: Maybe add a flag to force colors despite?
        if (nodeStyle.color != null && interpreter.evaluateAsBooleanOrNull(nodeStyle.color) != null)
          return InterceptionResult.DO_PROCESS;
      }

      if (node instanceof TextNode) {
        TextNode textNode = (TextNode) node;
        NodeStyle textStyle = textNode.getStyle();

        String nodeText = interpreter.evaluateAsString(textNode.text);
        StringBuilder whitespaceAccumulator = new StringBuilder();

        for (int charIndex = 0; charIndex < nodeText.length(); ++charIndex) {
          char currentChar = nodeText.charAt(charIndex);

          if (Character.isWhitespace(currentChar)) {
            whitespaceAccumulator.append(currentChar);
            continue;
          }

          if (whitespaceAccumulator.length() > 0) {
            TextNode whitespaceNode = new TextNode(ImmediateExpression.of(whitespaceAccumulator.toString()), node.position, null);

            if (textStyle != null)
              whitespaceNode.getOrInstantiateStyle().inheritFrom(textStyle);

            builder.onContent(whitespaceNode);
            whitespaceAccumulator.setLength(0);
          }

          TextNode charNode = new TextNode(ImmediateExpression.of(String.valueOf(currentChar)), node.position, null);

          if (textStyle != null)
            charNode.getOrInstantiateStyle().inheritFrom(textStyle);

          Object charComponent = builder.onContent(charNode);
          injectedNodes.add(charComponent);
        }

        if (whitespaceAccumulator.length() > 0) {
          TextNode whitespaceNode = new TextNode(ImmediateExpression.of(whitespaceAccumulator.toString()), node.position, null);

          if (textStyle != null)
            whitespaceNode.getOrInstantiateStyle().inheritFrom(textStyle);

          builder.onContent(whitespaceNode);
        }

        return InterceptionResult.DO_NOT_PROCESS;
      }

      Object otherComponent = builder.onContent((ContentNode) node);
      injectedNodes.add(otherComponent);

      return InterceptionResult.DO_NOT_PROCESS;
    }

    return InterceptionResult.DO_PROCESS;
  }

  @Override
  public void afterInterpretation(MarkupNode node, Interpreter interpreter) {
    if (!(node instanceof GradientNode))
      return;

    GradientNodeState state = getState(interpreter);

    List<Object> injectedComponents = state.injectedComponentsStack.pop();

    int injectedComponentsCount = injectedComponents.size();

    for (int injectedComponentsIndex = 0; injectedComponentsIndex < injectedComponentsCount; ++injectedComponentsIndex) {
      Object injectedComponent = injectedComponents.get(injectedComponentsIndex);
      double progressionPercentage = ((injectedComponentsIndex + 1D) / injectedComponentsCount) * 100.0;
      Color currentColor = state.gradientGenerator.getColor(progressionPercentage);

      interpreter.getComponentConstructor().setColor(injectedComponent, new ModernColor(currentColor));
    }
  }

  @Override
  public void onSkippedByParent(MarkupNode node, Interpreter interpreter) {
    GradientNodeState state = getState(interpreter);

    if (!state.deep && interpreter.isInSubtree())
      return;

    if (node instanceof GradientNode)
      state.injectedComponentsStack.pop();
  }
}
