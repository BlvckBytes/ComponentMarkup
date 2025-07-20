package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.InfixOperationNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupLetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class MarkupNode {

  public @Nullable ExpressionNode ifCondition;
  public @Nullable ExpressionNode useCondition;

  public CursorPosition position;

  public @Nullable List<MarkupNode> children;
  public @Nullable Set<LetBinding> letBindings;

  public MarkupNode(
    CursorPosition position,
    @Nullable List<MarkupNode> children,
    @Nullable Set<LetBinding> letBindings
  ) {
    this.position = position;
    this.children = children;
    this.letBindings = letBindings;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean canBeUnpackedFromAndIfSoInherit(MarkupNode other) {
    // Do not add additional bindings (which would not be included) to a capture
    if (other.letBindings != null) {
      for (LetBinding letBinding : other.letBindings) {
        if (letBinding instanceof MarkupLetBinding && ((MarkupLetBinding) letBinding).capture)
          return false;
      }
    }

    if (other instanceof StyledNode) {
      NodeStyle otherStyle = ((StyledNode) other).getStyle();

      if (otherStyle != null && otherStyle.hasNonNullProperties()) {
        if (!(this instanceof StyledNode))
          return false;

        ((StyledNode) this).getOrInstantiateStyle().inheritFrom(otherStyle, other.useCondition);
      }
    }

    if (other.ifCondition != null) {
      if (this.ifCondition == null)
        this.ifCondition = other.ifCondition;
      else
        this.ifCondition = new InfixOperationNode(this.ifCondition, InfixOperator.CONJUNCTION, other.ifCondition, null);
    }

    if (other.letBindings != null && !other.letBindings.isEmpty()) {
      if (this.letBindings == null)
        this.letBindings = other.letBindings;
      else
        this.letBindings.addAll(other.letBindings);
    }

    return true;
  }
}
