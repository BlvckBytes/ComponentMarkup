package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.ArrayList;
import java.util.List;

public class TagAndBuffers {

  public final TagDefinition tag;
  public final String tagNameLower;
  public final CursorPosition position;

  public final List<LetBinding> bindings;
  public final List<Attribute> attributes;
  public final List<AstNode> children;

  public TagAndBuffers(TagDefinition tag, String tagNameLower, CursorPosition position) {
    this.tag = tag;
    this.tagNameLower = tagNameLower;
    this.position = position;

    this.bindings = new ArrayList<>();
    this.attributes = new ArrayList<>();
    this.children = new ArrayList<>();
  }

  public AstNode construct() {
    return tag.construct(
      tagNameLower,
      // TODO: Implement
      false,
      position,
      attributes, bindings, children
    );
  }
}
