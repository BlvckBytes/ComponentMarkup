package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.node.style.Formatting;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class FormattingTag extends TagDefinition {

  @Override
  public boolean matchName(String tagName) {
    int nameLength = tagName.length();

    if (nameLength == 0)
      return false;

    char firstChar = tagName.charAt(0);

    if (nameLength == 2 && firstChar == '&') {
      char secondChar = tagName.charAt(1);

      return (
        (secondChar >= 'k' && secondChar <= 'o') ||
        (secondChar >= 'K' && secondChar <= 'O') ||
        secondChar == 'r' ||
        secondChar == 'R'
      );
    }

    switch (tagName) {
      case "b":
      case "bold":
      case "!b":
      case "!bold":
      case "i":
      case "italic":
      case "!i":
      case "!italic":
      case "u":
      case "underlined":
      case "!u":
      case "!underlined":
      case "st":
      case "strikethrough":
      case "!st":
      case "!strikethrough":
      case "obf":
      case "obfuscated":
      case "!obf":
      case "!obfuscated":
      case "reset":
        return true;
    }

    return false;
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
  public AttributeDefinition[] getAttributes() {
    return NO_ATTRIBUTES;
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    ContentNode wrapper = new ContentNode(position, children, letBindings);
    applyFormatting(tagName, wrapper.style);
    return wrapper;
  }

  private void applyFormatting(String tagName, NodeStyle style) {
    boolean isNegative = tagName.charAt(0) == '!';

    Formatting formatting;

    switch (tagName) {
      case "b":
      case "bold":
      case "!b":
      case "!bold":
        formatting = Formatting.BOLD;
        break;

      case "i":
      case "italic":
      case "!i":
      case "!italic":
        formatting = Formatting.ITALIC;
        break;

      case "u":
      case "underlined":
      case "!u":
      case "!underlined":
        formatting = Formatting.UNDERLINE;
        break;

      case "st":
      case "strikethrough":
      case "!st":
      case "!strikethrough":
        formatting = Formatting.STRIKETHROUGH;
        break;

      case "obf":
      case "obfuscated":
      case "!obf":
      case "!obfuscated":
        formatting = Formatting.MAGIC;
        break;

      case "reset":
        style.reset();
        return;

      default:
        return;
    }

    if (isNegative) {
      style.disableFormatting(formatting);
      return;
    }

    style.enableFormatting(formatting);
  }
}
