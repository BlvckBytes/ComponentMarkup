package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.style.Format;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class FormatTag extends TagDefinition {

  public FormatTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("obfuscated", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("strikethrough", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("underlined", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("italic", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("bold", AttributeType.EXPRESSION, false, false)
      }
    );
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equals("format");
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.OPEN_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    applyFormat(attributes, wrapper.style);
    return wrapper;
  }

  private void applyFormat(List<Attribute> attributes, NodeStyle style) {
    for (Attribute attribute : attributes) {
      Format targetFormat;

      switch (attribute.name) {
        case "obfuscated":
          targetFormat = Format.MAGIC;
          break;

        case "bold":
          targetFormat = Format.BOLD;
          break;

        case "strikethrough":
          targetFormat = Format.STRIKETHROUGH;
          break;

        case "underlined":
          targetFormat = Format.UNDERLINED;
          break;

        case "italic":
          targetFormat = Format.ITALIC;
          break;

        default:
          continue;
      }

      if (!(attribute instanceof ExpressionAttribute))
        throw new IllegalStateException("Expected attribute '" + attribute.name + "' to be an expression");

      style.formatStates[targetFormat.ordinal()] = ((ExpressionAttribute) attribute).value;
    }
  }
}
