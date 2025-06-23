package at.blvckbytes.component_markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.PendingTextNode;
import at.blvckbytes.component_markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.interpreter.InterceptionResult;
import at.blvckbytes.component_markup.interpreter.InterpreterInterceptor;
import at.blvckbytes.component_markup.interpreter.OutputBuilder;
import at.blvckbytes.component_markup.interpreter.TemporaryMemberEnvironment;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GradientNode extends AstNode implements InterpreterInterceptor {

  private final ThreadLocal<Stack<List<NodeAndChar>>> threadLocalPendingNodes = ThreadLocal.withInitial(Stack::new);

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
  public InterceptionResult interceptInterpretation(
    AstNode node,
    OutputBuilder builder,
    TemporaryMemberEnvironment environment,
    IExpressionEvaluator expressionEvaluator
  ) {
    if (node instanceof GradientNode) {
      threadLocalPendingNodes.get().push(new ArrayList<>());
      return InterceptionResult.PROCESS_DO_CALL_AFTER;
    }

    List<NodeAndChar> pendingNodes = threadLocalPendingNodes.get().peek();

    if (node instanceof TextNode) {
      NodeStyle nodeStyle = ((TextNode) node).getStyle();

      if (nodeStyle != null && nodeStyle.color != ImmediateExpression.ofNull())
        return InterceptionResult.PROCESS_DO_NOT_CALL_AFTER;

      Object nodeValue = expressionEvaluator.evaluateExpression(((TextNode) node).text, environment);
      String nodeText = environment.getValueInterpreter().asString(nodeValue);

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

        PendingTextNode charNode = new PendingTextNode(node.position, null);

        pendingNodes.add(new NodeAndChar(charNode, currentChar));

        builder.onContent(charNode);
      }

      if (whitespaceAccumulator.length() > 0)
        builder.onContent(new TextNode(ImmediateExpression.of(whitespaceAccumulator.toString()), node.position, null));

      return InterceptionResult.DO_NOT_PROCESS;
    }

    return InterceptionResult.PROCESS_DO_NOT_CALL_AFTER;
  }

  @Override
  public void afterInterpretation(AstNode node, OutputBuilder builder, TemporaryMemberEnvironment environment, IExpressionEvaluator expressionEvaluator) {
    if (node instanceof GradientNode) {
      List<NodeAndChar> pendingNodes = threadLocalPendingNodes.get().pop();

      int pendingNodeCount = pendingNodes.size();

      for (int pendingNodeIndex = 0; pendingNodeIndex < pendingNodeCount; ++pendingNodeIndex) {
        NodeAndChar pendingNode = pendingNodes.get(pendingNodeIndex);
        NodeStyle permanentStyle = pendingNode.node.getStyle();
        NodeStyle temporaryStyle = new NodeStyle();

        if (permanentStyle != null)
          temporaryStyle.inheritFrom(permanentStyle);

        // TODO: Compute color based on progression as well as start, end and intermediate colors
        double gradientProgression = (pendingNodeIndex + 1D) / pendingNodeCount;
        temporaryStyle.color = ImmediateExpression.of("#000000");

        pendingNode.node.notify(String.valueOf(pendingNode.character), temporaryStyle);
      }
    }
  }
}
