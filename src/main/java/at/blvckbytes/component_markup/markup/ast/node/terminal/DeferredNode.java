package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class DeferredNode<Parameter extends RendererParameter> extends UnitNode implements DeferredRenderer<Parameter> {

  public DeferredNode(int position, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(position, letBindings);
  }

  public abstract Parameter createParameter(Interpreter interpreter);

}
