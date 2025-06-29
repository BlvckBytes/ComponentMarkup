package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class TagDefinition {

  public final Set<AttributeDefinition> attributes;
  public final Collection<String> staticPrefixes;
  public final TagClosing tagClosing;
  public final TagPriority tagPriority;

  protected TagDefinition(
    String[] staticPrefixes,
    TagClosing tagClosing,
    TagPriority tagPriority,
    AttributeDefinition... attributes
  ) {
    this.attributes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(attributes)));
    this.staticPrefixes = Collections.unmodifiableList(Arrays.asList(staticPrefixes));
    this.tagClosing = tagClosing;
    this.tagPriority = tagPriority;
  }

  public @Nullable AttributeDefinition getAttribute(String attributeName) {
    for (AttributeDefinition attribute : attributes) {
      if (attribute.name.equalsIgnoreCase(attributeName))
        return attribute;
    }

    return null;
  }

  public abstract boolean matchName(String tagNameLower);

  public abstract @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  );
}
