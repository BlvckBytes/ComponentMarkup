package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.transition;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.UnitNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ColorizeMonochromeNode extends ColorizeNode {

  public ColorizeMonochromeNode(
    String tagNameLower,
    Function<Interpreter, ColorizeNodeState> stateCreator,
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable Set<LetBinding> letBindings
  ) {
    super(tagNameLower, stateCreator, position, children, letBindings);
  }

  @Override
  protected boolean handleTextAndGetIfDoProcess(TextNode node, ColorizeNodeState state, Interpreter interpreter) {
    interpreter.getCurrentBuilder().onText(node, state::addInjected, false);
    return false;
  }

  @Override
  protected boolean handleUnitAndGetIfDoProcess(UnitNode node, ColorizeNodeState state, Interpreter interpreter) {
    interpreter.getCurrentBuilder().onUnit(node, state::addInjected);
    return false;
  }
}
