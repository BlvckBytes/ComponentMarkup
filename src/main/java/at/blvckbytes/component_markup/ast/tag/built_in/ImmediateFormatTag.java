package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.style.Format;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImmediateFormatTag extends TagDefinition {

  private static final Set<String> namedFormats;
  private static final String[] staticPrefixes;

  static {
    namedFormats = new HashSet<>();
    namedFormats.add("b");
    namedFormats.add("bold");
    namedFormats.add("!b");
    namedFormats.add("!bold");
    namedFormats.add("i");
    namedFormats.add("italic");
    namedFormats.add("!i");
    namedFormats.add("!italic");
    namedFormats.add("u");
    namedFormats.add("underlined");
    namedFormats.add("!u");
    namedFormats.add("!underlined");
    namedFormats.add("st");
    namedFormats.add("strikethrough");
    namedFormats.add("!st");
    namedFormats.add("!strikethrough");
    namedFormats.add("obf");
    namedFormats.add("obfuscated");
    namedFormats.add("!obf");
    namedFormats.add("!obfuscated");
    namedFormats.add("reset");

    staticPrefixes = new String[namedFormats.size() + 1];

    int prefixIndex = 0;

    for (String namedFormat : namedFormats)
      staticPrefixes[prefixIndex++] = namedFormat;

    staticPrefixes[prefixIndex] = "&";
  }

  public ImmediateFormatTag() {
    super(NO_ATTRIBUTES, staticPrefixes, TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  private boolean isFormatChar(char c, boolean allowReset) {
    return (
      (c >= 'k' && c <= 'o') ||
      (c >= 'K' && c <= 'O') ||
      (allowReset && (c == 'r' || c == 'R'))
    );
  }

  @Override
  public boolean matchName(String tagName) {
    int nameLength = tagName.length();

    if (nameLength == 0)
      return false;

    char firstChar = tagName.charAt(0);

    if (nameLength == 2 && firstChar == '&')
      return isFormatChar(tagName.charAt(1), true);

    if (nameLength == 3 && firstChar == '&' && tagName.charAt(1) == '!')
      return isFormatChar(tagName.charAt(2), false);

    return namedFormats.contains(tagName);
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
    applyFormat(tagName, wrapper.style);
    return wrapper;
  }

  private void applyFormat(String tagName, NodeStyle style) {
    boolean isNegative = tagName.charAt(0) == '!' || tagName.charAt(1) == '!';

    Format format;

    switch (tagName) {
      case "&l":
      case "&!l":
      case "b":
      case "bold":
      case "!b":
      case "!bold":
        format = Format.BOLD;
        break;

      case "&o":
      case "&!o":
      case "i":
      case "italic":
      case "!i":
      case "!italic":
        format = Format.ITALIC;
        break;

      case "&n":
      case "&!n":
      case "u":
      case "underlined":
      case "!u":
      case "!underlined":
        format = Format.UNDERLINED;
        break;

      case "&m":
      case "&!m":
      case "st":
      case "strikethrough":
      case "!st":
      case "!strikethrough":
        format = Format.STRIKETHROUGH;
        break;

      case "&k":
      case "&!k":
      case "obf":
      case "obfuscated":
      case "!obf":
      case "!obfuscated":
        format = Format.MAGIC;
        break;

      case "&r":
      case "reset":
        style.reset();
        return;

      default:
        return;
    }

    style.formatStates[format.ordinal()] = ImmediateExpression.of(!isNegative);
  }
}
