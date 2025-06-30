package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.markup.ast.node.content.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.util.JsonifyIgnore;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ColorizeNode extends MarkupNode implements InterpreterInterceptor {

  @JsonifyIgnore
  private final ThreadLocal<@Nullable ColorizeNodeState> threadLocalState = ThreadLocal.withInitial(() -> null);

  @JsonifyIgnore
  private final Function<Interpreter, ColorizeNodeState> stateCreator;

  public final String tagNameLower;

  public ColorizeNode(
    String tagNameLower,
    Function<Interpreter, ColorizeNodeState> stateCreator,
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.tagNameLower = tagNameLower;
    this.stateCreator = stateCreator;
  }

  private ColorizeNodeState getState(Interpreter interpreter) {
    ColorizeNodeState state;

    if ((state = threadLocalState.get()) != null)
      return state;

    state = stateCreator.apply(interpreter);

    threadLocalState.set(state);
    return state;
  }

  @Override
  public InterceptionResult interceptInterpretation(MarkupNode node, Interpreter interpreter) {
    ColorizeNodeState state = getState(interpreter);

    if (!state.flags.contains(ColorizeFlag.DEEP) && interpreter.isInSubtree())
      return InterceptionResult.DO_PROCESS;

    if (node instanceof ColorizeNode && state.doesTargetNode((ColorizeNode) node)) {
      state.begin();
      return InterceptionResult.DO_PROCESS_AND_CALL_AFTER;
    }

    if (node instanceof ContentNode) {
      OutputBuilder builder = interpreter.getCurrentBuilder();
      NodeStyle nodeStyle = ((ContentNode) node).getStyle();

      if (nodeStyle != null) {
        if (!state.flags.contains(ColorizeFlag.OVERRIDE_COLORS) && nodeStyle.color != null && interpreter.evaluateAsBooleanOrNull(nodeStyle.color) != null)
          return InterceptionResult.DO_PROCESS;
      }

      if (node instanceof TextNode) {
        TextNode textNode = (TextNode) node;

        boolean skipWhitespace = state.flags.contains(ColorizeFlag.SKIP_WHITESPACE);
        String nodeText = interpreter.evaluateAsString(textNode.text);
        StringBuilder whitespaceAccumulator = skipWhitespace ? new StringBuilder() : null;

        for (int charIndex = 0; charIndex < nodeText.length(); ++charIndex) {
          char currentChar = nodeText.charAt(charIndex);

          if (Character.isWhitespace(currentChar)) {
            if (skipWhitespace) {
              whitespaceAccumulator.append(currentChar);
              continue;
            }
          }

          if (skipWhitespace)
            emitWhitespace(textNode, builder, whitespaceAccumulator);

          TextNode charNode = new TextNode(ImmediateExpression.of(String.valueOf(currentChar)), node.position, node.letBindings);

          if (nodeStyle != null)
            charNode.getOrInstantiateStyle().inheritFrom(nodeStyle);

          state.addInjected(builder.onContent(charNode));
        }

        if (skipWhitespace)
          emitWhitespace(textNode, builder, whitespaceAccumulator);

        return InterceptionResult.DO_NOT_PROCESS;
      }

      if (state.flags.contains(ColorizeFlag.SKIP_NON_TEXT))
        return InterceptionResult.DO_PROCESS;

      state.addInjected(builder.onContent((ContentNode) node));

      return InterceptionResult.DO_NOT_PROCESS;
    }

    return InterceptionResult.DO_PROCESS;
  }

  private void emitWhitespace(StyledNode styleHolder, OutputBuilder builder, StringBuilder accumulator) {
    if (accumulator.length() == 0)
      return;

    TextNode whitespaceNode = new TextNode(ImmediateExpression.of(accumulator.toString()), styleHolder.position, null);
    NodeStyle nodeStyle = styleHolder.getStyle();

    if (nodeStyle != null)
      whitespaceNode.getOrInstantiateStyle().inheritFrom(nodeStyle);

    builder.onContent(whitespaceNode);
    accumulator.setLength(0);
  }

  @Override
  public void afterInterpretation(MarkupNode node, Interpreter interpreter) {
    ColorizeNodeState state = getState(interpreter);

    if (!(node instanceof ColorizeNode && state.doesTargetNode((ColorizeNode) node)))
      return;

    state.end(interpreter);
  }

  @Override
  public void onSkippedByParent(MarkupNode node, Interpreter interpreter) {
    ColorizeNodeState state = getState(interpreter);

    if (!(node instanceof ColorizeNode && state.doesTargetNode((ColorizeNode) node)))
      return;

    if (!state.flags.contains(ColorizeFlag.DEEP) && interpreter.isInSubtree())
      return;

    state.discard();
  }
}
