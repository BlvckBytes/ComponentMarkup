package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.DelayedCreationHandler;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.OutputBuilder;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ColorizeCharsNode extends ColorizeNode {

  public ColorizeCharsNode(
    String tagNameLower,
    Function<Interpreter, ColorizeNodeState> stateCreator,
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable List<LetBinding> letBindings
  ) {
    super(tagNameLower, stateCreator, position, children, letBindings);
  }

  @Override
  protected boolean handleTerminalAndGetIfDoProcess(TerminalNode node, ColorizeNodeState state, Interpreter interpreter) {
    OutputBuilder builder = interpreter.getCurrentBuilder();
    NodeStyle nodeStyle = node.getStyle();

    if (node instanceof TextNode) {
      String text = ((TextNode) node).text;

      boolean skipWhitespace = state.flags.contains(ColorizeFlag.SKIP_WHITESPACE);
      StringBuilder whitespaceAccumulator = skipWhitespace ? new StringBuilder() : null;

      for (int charIndex = 0; charIndex < text.length(); ++charIndex) {
        char currentChar = text.charAt(charIndex);

        if (Character.isWhitespace(currentChar)) {
          if (skipWhitespace) {
            whitespaceAccumulator.append(currentChar);
            continue;
          }
        }

        if (skipWhitespace)
          emitWhitespace(node, builder, whitespaceAccumulator);

        TextNode charNode = new TextNode(String.valueOf(currentChar), node.position);

        if (nodeStyle != null)
          charNode.getOrInstantiateStyle().inheritFrom(nodeStyle, null);

        addInjected(state, builder, charNode);
      }

      if (skipWhitespace)
        emitWhitespace(node, builder, whitespaceAccumulator);

      return false;
    }

    if (state.flags.contains(ColorizeFlag.SKIP_NON_TEXT))
      return true;

    addInjected(state, builder, node);
    return false;
  }

  private void emitWhitespace(StyledNode styleHolder, OutputBuilder builder, StringBuilder accumulator) {
    if (accumulator.length() == 0)
      return;

    TextNode whitespaceNode = new TextNode(accumulator.toString(), styleHolder.position);
    NodeStyle nodeStyle = styleHolder.getStyle();

    if (nodeStyle != null)
      whitespaceNode.getOrInstantiateStyle().inheritFrom(nodeStyle, null);

    // No need to delay creation, as there will be a differently colorized node right after
    builder.onTerminal(whitespaceNode, DelayedCreationHandler.NONE_SENTINEL);
    accumulator.setLength(0);
  }
}
