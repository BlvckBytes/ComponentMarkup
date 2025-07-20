package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class DeferredNode<Parameter extends RendererParameter> extends UnitNode implements DeferredRenderer<Parameter> {

  public DeferredNode(CursorPosition position, @Nullable Set<LetBinding> letBindings) {
    super(position, letBindings);
  }

  public abstract Parameter createParameter(Interpreter interpreter);

}
