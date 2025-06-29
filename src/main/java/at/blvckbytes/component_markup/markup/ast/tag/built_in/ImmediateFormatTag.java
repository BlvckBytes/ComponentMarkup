package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.TagClosing;
import at.blvckbytes.component_markup.markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.markup.ast.tag.TagPriority;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

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
    super(staticPrefixes, TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  private boolean isFormatChar(char c, boolean allowReset) {
    return (
      (c >= 'k' && c <= 'o') ||
      (allowReset && c == 'r')
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    int nameLength = tagNameLower.length();

    if (nameLength == 0)
      return false;

    char firstChar = tagNameLower.charAt(0);

    if (nameLength == 2 && firstChar == '&')
      return isFormatChar(tagNameLower.charAt(1), true);

    if (nameLength == 3 && firstChar == '&' && tagNameLower.charAt(1) == '!')
      return isFormatChar(tagNameLower.charAt(2), false);

    return namedFormats.contains(tagNameLower);
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    applyFormat(tagNameLower, wrapper.getOrInstantiateStyle());
    return wrapper;
  }

  private void applyFormat(String tagNameLower, NodeStyle style) {
    boolean isNegative = tagNameLower.charAt(0) == '!' || tagNameLower.charAt(1) == '!';

    Format format;

    switch (tagNameLower) {
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
        format = Format.OBFUSCATED;
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
