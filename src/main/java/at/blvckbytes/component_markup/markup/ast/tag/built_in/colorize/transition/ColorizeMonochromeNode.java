package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.transition;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.OutputBuilder;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ColorizeMonochromeNode extends ColorizeNode {

  public ColorizeMonochromeNode(
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
    if (!(node instanceof TextNode)) {
      if (state.flags.contains(ColorizeFlag.SKIP_NON_TEXT))
        return true;
    }

    OutputBuilder builder = interpreter.getCurrentBuilder();
    addInjected(state, builder, node);
    return false;
  }
}
