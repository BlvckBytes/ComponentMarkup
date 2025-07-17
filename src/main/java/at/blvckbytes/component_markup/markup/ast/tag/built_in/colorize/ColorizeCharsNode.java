package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.node.terminal.UnitNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
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
  protected boolean handleTextAndGetIfDoProcess(TextNode node, ColorizeNodeState state, Interpreter interpreter) {
    OutputBuilder builder = interpreter.getCurrentBuilder();
    NodeStyle nodeStyle = node.getStyle();

    boolean skipWhitespace = state.flags.contains(ColorizeFlag.SKIP_WHITESPACE);
    StringBuilder whitespaceAccumulator = skipWhitespace ? new StringBuilder() : null;

    for (int charIndex = 0; charIndex < node.text.length(); ++charIndex) {
      char currentChar = node.text.charAt(charIndex);

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

      // No need to buffer, as all chars will be colored differently
      builder.onText(charNode, state::addInjected, true);
    }

    if (skipWhitespace)
      emitWhitespace(node, builder, whitespaceAccumulator);

    return false;
  }

  @Override
  protected boolean handleUnitAndGetIfDoProcess(UnitNode node, ColorizeNodeState state, Interpreter interpreter) {
    interpreter.getCurrentBuilder().onUnit(node, state::addInjected);
    return false;
  }

  private void emitWhitespace(StyledNode styleHolder, OutputBuilder builder, StringBuilder accumulator) {
    if (accumulator.length() == 0)
      return;

    TextNode whitespaceNode = new TextNode(accumulator.toString(), styleHolder.position);
    NodeStyle nodeStyle = styleHolder.getStyle();

    if (nodeStyle != null)
      whitespaceNode.getOrInstantiateStyle().inheritFrom(nodeStyle, null);

    // No need to buffer, as all chars will be colored differently
    builder.onText(whitespaceNode, null, true);
    accumulator.setLength(0);
  }
}
