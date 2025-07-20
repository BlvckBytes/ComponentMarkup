package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class TagDefinition {

  public final TagClosing tagClosing;
  public final TagPriority tagPriority;

  protected TagDefinition(
    TagClosing tagClosing,
    TagPriority tagPriority
  ) {
    this.tagClosing = tagClosing;
    this.tagPriority = tagPriority;
  }

  public abstract boolean matchName(String tagNameLower);

  public abstract @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable Set<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  );
}
