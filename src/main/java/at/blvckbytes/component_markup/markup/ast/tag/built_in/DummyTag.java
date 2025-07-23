package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class DummyTag extends TagDefinition {

  public static final DummyTag INSTANCE = new DummyTag();

  private DummyTag() {
    super(TagClosing.INVARIANT, TagPriority.LOWEST);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return true;
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ((InternalAttributeMap) attributes).markAllUsed();
    return new ContainerNode(position, children, letBindings);
  }
}
